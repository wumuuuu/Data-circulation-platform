import CryptoJS from 'crypto-js';
import {modularExponentiation, p, g} from '../utils/CountKey.js'
import { Application, startSignProcess, submitSignature } from '@/api/api.js'
import { signTaskId } from '@/service/wsService.js'
import { ref } from 'vue'

let dataId = 0;

// 开启签名流程
export const OpenTheSignProcess = async (form) => {
  let key = form.value.keyList[0];
  key = BigInt(await readFileContent(key));

  const H_hex = await hash_m(form);
  const H = BigInt(`0x${H_hex}`);

  const x = modularExponentiation(H, BigInt(2), p);
  const y = modularExponentiation(x, key, p);
  const b = modularExponentiation(g, key, p);

  const participants = form.value.signer.map(signer => ({
    username: signer.username,
    role: signer.role,
  }));

  try {
    // 调用后端接口启动签名流程
    const startResponse = await startSignProcess({
      dataId: dataId.toString(),
      participants: participants,
      y: y.toString(),
      b: b.toString(),
    });

    const taskId = startResponse.taskId;
    console.log('Task ID from server:', taskId);

  //   // 上传文件
  //   if (form.value.file) {
  //     await uploadFile(taskId, form.value.file);
  //   }

    console.log('Sign process started successfully');
  } catch (error) {
    console.error('Error starting sign process:', error);
  }
};

//用户进行签名计算
export const CountKey = async (file) =>{
  let key = await readFileContent(file);
  key = BigInt(key);
  let y = BigInt(localStorage.getItem('signTaskY'));
  let b = BigInt(localStorage.getItem('signTaskB'));

  y = modularExponentiation(y, key, p);
  b = modularExponentiation(b, key, p);

  console.log("y = " + y.toString());
  console.log("b = " + b.toString());

  try {
    // 调用后端接口启动签名流程
    const startResponse = await submitSignature ({
      taskId: localStorage.getItem('signTaskId'),
      y: y.toString(),
      b: b.toString(),
    });

    console.log('签名提交成功');
  } catch (error) {
    console.error('签名提交出错:', error);
  }
}

//处理消息
export const SignProcess= async (messageData) =>{
  if (messageData.y && messageData.b) {
    localStorage.setItem('signTaskY', messageData.y);
    localStorage.setItem('signTaskB', messageData.b);
    console.log('y :', messageData.y + ' b : ' + messageData.b);
  } else if (messageData.taskStatus === 'complete') {
    if(localStorage.getItem('ApplicationId')) {
      const applicationData = {
        id: localStorage.getItem('ApplicationId'),
        applicationStatus: 'END', // 确保与后端的字段名一致
        username: null, // 添加必要的字段
        applicationType: null,
      };
      try {
        //更新申请为END
        await Application(applicationData);
      } catch (error) {
        console.error('Error approving application:', error);
      }
    }
    localStorage.removeItem('signTaskId');
    localStorage.removeItem('signTaskY');
    localStorage.removeItem('signTaskB');
    console.log("签名已完成");
  } else if (messageData.taskId) {
    console.log(messageData);
    signTaskId.value = messageData.taskId;
    localStorage.setItem('signTaskId', messageData.taskId);
    console.log('Received signTaskId:', messageData.taskId);
  }
}

// 计算 hash_m
export const hash_m = async (form) => {
  let file = form.value.fileList[0]; // 获取第一个文件
  let signers = form.value.signer;   // 获取签名者列表

  // 读取文件内容
  let content = await readFileContent(file);
  dataId = CryptoJS.SHA256(content);
  // 拼接签名者的用户名
  for (let signer of signers) {
    if (signer.username) {
      content += signer.username;
    }
  }

  // 计算拼接字符串的哈希值
  const hash = CryptoJS.SHA256(content);  // 使用拼接后的 content
  const H = hash.toString(CryptoJS.enc.Hex);  // 将哈希值转换为十六进制字符串
  return H;
};

// 辅助函数：读取文件内容并返回为字符串
const readFileContent = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = () => reject(reader.error);
    reader.readAsText(file); // 将文件内容读取为文本
  });
};

