// Examine1Service.js

import { get, post } from '@/utils/request.js'; // 导入用于发送请求的函数
import { ElMessage } from 'element-plus';
import { getSharedKey } from '@/utils/cryptoUtils.js';
import { decryptFile, encryptFile } from '@/service/cryptoWorkerService.js';
import { saveAs } from 'file-saver';

export const onSubmit = async (formData) => {

  const response = await post('/task/create', formData);

  if (response.success) {
    ElMessage.success('签名任务已创建');
  } else {
    ElMessage.error('签名任务创建失败');
  }
}

export const addMember = async (memberSearch, signer) => {
  if (memberSearch && signer.members.find(m => m.name === memberSearch)) {
    ElMessage.error('用户名已在列表');
    return;
  }
  // 发送请求到服务器，检查用户名是否已存在
  const response = await post('/find-username', { username: memberSearch });

  if (response.success) {
    // 如果用户名不存在，无法添加到签名人列表
    ElMessage.error('用户名不存在');
  } else {
    signer.members.push({ username: memberSearch });
    ElMessage.success('用户已添加');
  }
};

export async function fetchFiles(creatorName) {
  try {

    // 在请求中传递 creatorName 作为查询参数
    const response = await get(`/files?creatorName=${encodeURIComponent(creatorName)}`);

    // 检查响应是否成功
    if (response.success) {
      return response.data;
    } else {
      throw new Error(response.message || 'Failed to fetch files');
    }
  } catch (error) {
    console.error('Error fetching files:', error);
    throw error; // 抛出错误，便于调用方捕获
  }
}


/**
 * 获取需要数据所有方审核的申请记录
 * @returns {Promise<Array<Object>>} 返回包含该用户所有申请记录的数组
 */
export async function fetchApplications() {
  try {
    const response = await get('/application/pending1');
    if (response.success) {
      return response.data;
    } else {
      throw new Error(response.message || 'Failed to fetch pending applications');
    }
  } catch (error) {
    // 判断错误类型
    if (error.message === '未找到等待数据所有方审核的申请记录') {
      ElMessage.success('暂无数据');
    } else {
      ElMessage.error('获取等待数据所有方审核的申请记录失败');
    }

    console.error('Error fetching pending applications:', error);
    throw error;
  }
}

const getFileId = async () => {
  try {
    const response = await get('/start-upload');

    if (response.success) {
      return response.data;
    } else {
      throw new Error(response.message || '获取文件ID失败');
    }
  } catch (error) {

    console.error('获取文件ID时发生错误:', error);
    throw error;
  }
};

// 加密文件，并更新进度
export const encryptCsvFileWithProgress = async (file, startTime, estimatedTime, progress, fileName, creator_name) => {
  const chunkSize = 1024 * 1024 * 10; // 每次处理 10MB

  const FileId = await getFileId();

  try {
    // 获取共享密钥
    const sharedSecret = await getSharedKey();

    const fileSize = file.size; // 文件总大小（字节）

    // 启动 Web Worker 处理文件加密
    const worker = new Worker(new URL('@/utils/cryptoWorker.js', import.meta.url));

    worker.onmessage = async function (e) {
      const { type, chunkWithLength, currentChunk, totalChunks, progress: progressValue } = e.data;

      if (type === 'encryptedChunk') {

        // 在使用 await 之前，立即复制所有需要的变量
        const localChunkWithLength = chunkWithLength.slice(0); // 对于 ArrayBuffer，使用 slice 复制
        const localCurrentChunk = currentChunk;
        const localTotalChunks = totalChunks;
        const localProgressValue = progressValue;

        // 接收到加密块，上传到服务器
        try {
          // console.log(`Main thread: Received chunk ${localCurrentChunk} of ${localTotalChunks}`);

          await uploadEncryptedChunk(localChunkWithLength, localCurrentChunk, localTotalChunks, FileId, fileName, creator_name);

          // 更新进度
          progress.value = Number(localProgressValue);

          // // 使用局部变量
          // console.log(`Uploaded chunk ${localCurrentChunk} of ${localTotalChunks}`);

          // 计算经过的时间
          const elapsedTime = (Date.now() - startTime) / 1000; // 秒

          // 已处理的数据量
          const processedData = (progress.value / 100) * fileSize;

          // 处理速度（字节/秒）
          const processingSpeed = processedData / elapsedTime;

          // 预估剩余时间 = 剩余数据量 / 处理速度
          const remainingData = fileSize - processedData;
          const remainingTime = remainingData / processingSpeed; // 以秒为单位

          // 更新 estimatedTime.value
          estimatedTime.value = formatTime(Math.max(remainingTime, 0)); // 确保剩余时间不为负数
        } catch (uploadError) {
          console.error(`Error uploading chunk ${localCurrentChunk}:`, uploadError);
        }
      } else if (type === 'done') {
        ElMessage.success('文件加密完成');
        console.log('All chunks have been encrypted and uploaded successfully.');
      }
    };

    // 向 Web Worker 发送加密文件的消息
    worker.postMessage({
      type: 'encryptFile',
      payload: {
        file: file,
        sharedSecret: sharedSecret,
        chunkSize: chunkSize,
      },
    });

  } catch (error) {
    ElMessage.error(`加密过程失败: ${error.message}`);
    console.error(`Encryption error:`, error);
    throw error;
  }
};

// 辅助函数：将 ArrayBuffer 转换为 Base64 字符串
function arrayBufferToBase641(buffer) {
  let binary = '';
  const bytes = new Uint8Array(buffer);
  const len = bytes.byteLength;
  for (let i = 0; i < len; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}

// 上传加密块到服务器的方法
async function uploadEncryptedChunk(chunk, currentChunk, totalChunks, fileId, fileName, creatorName) {

  // 创建 FormData 实例，用于存放要上传的块和其他元数据
  const formData = new FormData();

  // 添加加密块，类型为 'application/octet-stream' 表示为二进制数据
  formData.append('chunk', new Blob([chunk], { type: 'application/octet-stream' }));

  // 将块的索引（块编号）和总块数作为元数据传递给后端
  formData.append('chunkIndex', currentChunk - 1); // 当前上传的块编号
  formData.append('totalChunks', totalChunks); // 总的块数，便于后端知道这是第几块
  formData.append("fileId", fileId);
  formData.append("fileName", fileName);
  formData.append("creatorName", creatorName);
  // formData.append("fileOutline", fileOutline);

  try {

    const response = await fetch('/api/upload-chunk', {
      method: 'POST',
      body: formData,
    });

    const apiResponse = await response.json();

    // 如果上传成功，处理响应
    if (response && apiResponse.code === 200) {
      console.log(`Chunk ${currentChunk} of ${totalChunks} uploaded successfully.`);
    } else {
      // 上传失败时，记录错误信息
      console.error(`Failed to upload chunk ${currentChunk}: ${response.message || 'Unknown error'}`);
      throw new Error(`Failed to upload chunk ${currentChunk}: ${response.message || 'Unknown error'}`);
    }
  } catch (error) {
    // 捕获网络错误或其他异常情况
    console.error(`Error occurred while uploading chunk ${currentChunk}:`, error);

    // 如果有必要，可以在此添加重试逻辑或进一步的错误处理
    throw new Error(`Error uploading chunk ${currentChunk}: ${error.message}`);
  }
}


// 解密文件，并更新进度
export const decryptCsvFileWithProgress = async (encryptedDataBuffer, progressCallback) => {
  try {
    // 获取共享密钥
    const sharedSecret = await getSharedKey();

    // 调用解密函数
    const decryptedDataBuffer = await decryptFile(encryptedDataBuffer, sharedSecret, (progress) => {
      // 更新进度
      if (progressCallback) {
        progressCallback(progress);
      }
    });

    // 将解密后的数据保存为文件
    const decryptedBlob = new Blob([decryptedDataBuffer], { type: 'text/csv;charset=utf-8' });
    saveAs(decryptedBlob, 'decrypted.csv');

    ElMessage.success('文件解密完成');

  } catch (error) {
    ElMessage.error(`解密过程失败: ${error.message}`);
    throw error;
  }
};

// 保存解密后的 CSV 文件（如果需要）
export async function saveDecryptedCsvFile(decryptedBlob) {
  try {
    // 显示保存文件对话框
    const fileHandle = await window.showSaveFilePicker({
      suggestedName: 'data.csv',
      types: [
        {
          description: 'CSV Files',
          accept: {
            'text/csv': ['.csv'],
          },
        },
      ],
    });

    // 创建写入流
    const writableStream = await fileHandle.createWritable();

    // 将 Blob 数据写入文件
    await writableStream.write(decryptedBlob);

    // 关闭写入流
    await writableStream.close();

    console.log('CSV 文件已保存');
  } catch (error) {
    console.error('保存文件失败:', error.message);
  }
}

// 格式化剩余时间（秒转为 分钟:秒 格式）
const formatTime = (timeInSeconds) => {
  const minutes = Math.floor(timeInSeconds / 60);
  const seconds = Math.floor(timeInSeconds % 60);
  return `${minutes} 分 ${seconds} 秒`;
};
