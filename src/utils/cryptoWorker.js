// cryptoWorker.js

console.log('Web Worker is running');

// 处理来自主线程的消息事件，根据消息类型执行相应的加密或解密任务
onmessage = async function (e) {
  const { type, payload } = e.data;

  switch (type) {
    case 'generatePrivateKey':
      // 生成私钥，并将结果发送回主线程
      postMessage({
        type: 'privateKey',
        data: generatePrivateKey(),
      });
      break;

    case 'calculatePublicKey':
      // 计算公钥，并将结果发送回主线程
      const publicKey = calculatePublicKey(BigInt(payload.privateKey));
      postMessage({
        type: 'publicKey',
        data: publicKey,
      });
      break;

    case 'encryptData':
      // 加密数据，并将加密后的数据发送回主线程
      const encryptedData = await encryptData(payload.sharedSecret, payload.data);
      postMessage({
        type: 'encryptedData',
        data: encryptedData,
      });
      break;

    case 'decryptData':
      // 解密数据，并将解密后的数据发送回主线程
      const decryptedData = await decryptData(payload.sharedSecret, payload.encryptedData);
      postMessage({
        type: 'decryptedData',
        data: decryptedData,
      });
      break;

    case 'encryptFile':
      // 处理文件分块加密的逻辑
      await encryptFile(payload.file, payload.sharedSecret, payload.chunkSize);
      break;

    case 'decryptFile':
      // 处理文件分块解密的逻辑
      await decryptFile(payload.file, payload.sharedSecret);
      break;

    default:
      // 处理未知命令类型的情况
      postMessage({ type: 'error', message: 'Unknown command' });
  }
};

// 生成 1024 位的随机私钥
function generatePrivateKey() {
  const privateKey = CryptoJS.lib.WordArray.random(128).toString(CryptoJS.enc.Hex); // 1024 / 8 = 128 bytes
  return BigInt('0x' + privateKey);
}

// 计算公钥，使用大素数 p 和基数 g 进行模幂运算
export async function calculatePublicKey(privateKey) {
  const p = BigInt('132165373947571709001890899559578394061572732290158236845675979056783176833192189640519330577968623712019753279011546461561086378291703395170828826203868040544703192493236905634659492348075654172349595065574318562378095706622284475060330389667603958501055142626804746804365447731489915179943331725842802927799');
  const g = BigInt('436921');
  return modularExponentiation(g, privateKey, p);
}

// 模幂运算函数，用于计算 (base ^ exponent) % modulus
function modularExponentiation(base, exponent, modulus) {
  let result = BigInt(1);
  base = base % modulus;

  while (exponent > 0) {
    if (exponent % BigInt(2) === BigInt(1)) {
      result = (result * base) % modulus;
    }
    exponent = exponent >> BigInt(1); // 将指数右移一位（等同于除以2）
    base = (base * base) % modulus;
  }
  return result;
}

// 使用共享密钥加密数据（不可修改）
async function encryptData(sharedSecret, data) {
  const iv = crypto.getRandomValues(new Uint8Array(12)); // 生成随机 IV

  const encryptedData = await crypto.subtle.encrypt({
    name: 'AES-GCM',
    iv: iv
  }, sharedSecret, data);

  // 将 IV 和加密数据合并为一个 Uint8Array
  const encryptedDataWithIv = new Uint8Array(iv.length + encryptedData.byteLength);
  encryptedDataWithIv.set(iv); // 将 IV 放在前 12 字节
  encryptedDataWithIv.set(new Uint8Array(encryptedData), iv.length); // 将加密数据跟在 IV 之后

  // 返回 Base64 编码的字符串
  return arrayBufferToBase64(encryptedDataWithIv.buffer);
}

// 使用共享密钥解密数据（不可修改）
async function decryptData(sharedSecret, encryptedDataWithIvBase64) {
  const encryptedDataWithIv = base64ToArrayBuffer(encryptedDataWithIvBase64);

  // 将 ArrayBuffer 转换为 Uint8Array 以便使用 slice 方法分割 IV 和加密数据
  const encryptedDataArray = new Uint8Array(encryptedDataWithIv);
  const iv = encryptedDataArray.slice(0, 12); // 提取前 12 字节为 IV
  const encryptedData = encryptedDataArray.slice(12); // 剩余部分为加密数据

  // 解密数据
  const decryptedData = await crypto.subtle.decrypt({
    name: 'AES-GCM',
    iv: iv
  }, sharedSecret, encryptedData);

  // 返回解密后的字符串
  return new TextDecoder().decode(decryptedData);
}

// 加密文件
async function encryptFile(file, sharedSecret, chunkSize) {

  // 导出共享密钥为原始字节形式并输出其十六进制表示

  const totalChunks = Math.ceil(file.size / chunkSize); // 计算总块数
  // console.log(`Worker: file.size = ${file.size}, chunkSize = ${chunkSize}, totalChunks = ${totalChunks}`);

  let offset = 0;
  let currentChunk = 0;

  while (offset < file.size) {
    // 读取文件的一个块
    const chunkBlob = file.slice(offset, offset + chunkSize);
    const chunkArrayBuffer = await chunkBlob.arrayBuffer();

    // 加密数据
    const encryptedChunkBase64 = await encryptData(sharedSecret, new Uint8Array(chunkArrayBuffer));

    // 将加密结果从 Base64 转换回 ArrayBuffer
    const encryptedChunkArrayBuffer = base64ToArrayBuffer(encryptedChunkBase64);

    // 添加长度前缀（4 字节，表示加密块的长度）
    const lengthPrefix = new Uint32Array([encryptedChunkArrayBuffer.byteLength]);
    const lengthPrefixBuffer = lengthPrefix.buffer; // 4 字节

    // 合并长度前缀和加密块
    const chunkWithLength = concatenateArrayBuffers([lengthPrefixBuffer, encryptedChunkArrayBuffer]);

    // 更新块索引和偏移
    currentChunk += 1;
    offset += chunkSize;

    const progress = (currentChunk / totalChunks) * 100;

    // // 输出当前块的信息
    // console.log(`Worker: currentChunk = ${currentChunk}, totalChunks = ${totalChunks}, offset = ${offset}`);

    // 将加密块和进度发送回主线程，主线程负责上传
    postMessage({
      type: 'encryptedChunk',
      chunkWithLength,
      currentChunk,
      totalChunks,
      progress: progress.toFixed(2),
    }, [chunkWithLength]); // 使用 Transferable 对象来传递 ArrayBuffer
  }

  // 当所有块都加密完成后，通知主线程
  postMessage({ type: 'done', message: 'File encryption completed' });
}

// 解密文件（分块解密，主线程负责合并）
async function decryptFile(file, sharedSecret, chunkSize) {
  const totalChunks = Math.ceil(file.size / chunkSize);
  console.log(`Worker: file.size = ${file.size}, chunkSize = ${chunkSize}, totalChunks = ${totalChunks}`);

  let offset = 0;
  let currentChunk = 0;

  while (offset < file.size) {
    // 读取文件的一个块
    const chunkBlob = file.slice(offset, offset + chunkSize);
    const chunkArrayBuffer = await chunkBlob.arrayBuffer();

    // 解密数据
    const encryptedChunkBase64 = arrayBufferToBase64(chunkArrayBuffer);
    const decryptedChunkString = await decryptData(sharedSecret, encryptedChunkBase64);

    // 将解密后的字符串转换为 ArrayBuffer
    const decryptedChunkArrayBuffer = new TextEncoder().encode(decryptedChunkString);

    // 向主线程发送解密后的块
    postMessage({
      type: 'decryptedChunk',
      decryptedChunkArrayBuffer,
      currentChunk,
      totalChunks,
    }, [decryptedChunkArrayBuffer]); // 使用 Transferable 对象传递

    // 更新进度
    currentChunk += 1;
    const progress = (currentChunk / totalChunks) * 100;
    postMessage({
      type: 'progress',
      progress: progress.toFixed(2),
    });

    offset += chunkSize;
  }

  // 当所有块都解密完成后，通知主线程
  postMessage({ type: 'done', message: 'File decryption completed' });
}

// 辅助函数：连接多个 ArrayBuffer
function concatenateArrayBuffers(buffers) {
  let totalLength = buffers.reduce((sum, buffer) => sum + buffer.byteLength, 0);
  let temp = new Uint8Array(totalLength);
  let offset = 0;
  for (let buffer of buffers) {
    temp.set(new Uint8Array(buffer), offset);
    offset += buffer.byteLength;
  }
  return temp.buffer;
}

// 辅助函数：将 ArrayBuffer 转换为 Base64 字符串
function arrayBufferToBase64(buffer) {
  let binary = '';
  const bytes = new Uint8Array(buffer);
  const len = bytes.byteLength;
  for (let i = 0; i < len; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}

// 辅助函数：将 Base64 字符串转换为 ArrayBuffer
function base64ToArrayBuffer(base64) {
  const binaryString = atob(base64);
  const len = binaryString.length;
  const bytes = new Uint8Array(len);
  for (let i = 0; i < len; i++) {
    bytes[i] = binaryString.charCodeAt(i);
  }
  return bytes.buffer;
}
