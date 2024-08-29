// 导入外部脚本，包含 CryptoJS 库，用于加密操作
importScripts('https://cdnjs.cloudflare.com/ajax/libs/crypto-js/4.1.1/crypto-js.min.js');
console.log('Web Worker is running');
// 处理来自主线程的消息事件，根据消息类型执行相应的加密或解密任务
onmessage = async function (e) {
  const { type, payload } = e.data;

  switch (type) {
    case 'generatePrivateKey':
      // 生成私钥，并将结果发送回主线程
      postMessage({
        type: 'privateKey',
        result: generatePrivateKey(),
      });
      break;

    case 'calculatePublicKey':
      // 计算公钥，并将结果发送回主线程
      const publicKey = calculatePublicKey(BigInt(payload.privateKey));
      postMessage({
        type: 'publicKey',
        result: publicKey,
      });
      break;

    case 'encryptData':
      // 加密数据，并将加密后的数据发送回主线程
      const encryptedData = await encryptData(payload.sharedSecret, payload.data);
      postMessage({
        type: 'encryptedData',
        result: encryptedData,
      });
      break;

    case 'decryptData':
      // 解密数据，并将解密后的数据发送回主线程
      const decryptedData = await decryptData(payload.sharedSecret, payload.encryptedData);
      postMessage({
        type: 'decryptedData',
        result: decryptedData,
      });
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
function calculatePublicKey(privateKey) {
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

// 使用共享密钥加密数据，采用 AES-GCM 模式
async function encryptData(sharedSecret, data) {
  const iv = crypto.getRandomValues(new Uint8Array(12)); // 生成随机 IV
  const encryptedData = await crypto.subtle.encrypt({
    name: 'AES-GCM',
    iv: iv
  }, sharedSecret, new TextEncoder().encode(data));

  // 将 IV 和加密数据合并为一个 Uint8Array
  const encryptedDataWithIv = new Uint8Array(iv.length + encryptedData.byteLength);
  encryptedDataWithIv.set(iv);
  encryptedDataWithIv.set(new Uint8Array(encryptedData), iv.length);

  // 返回 Base64 编码的字符串
  return arrayBufferToBase64(encryptedDataWithIv.buffer);
}

// 使用共享密钥解密数据，采用 AES-GCM 模式
async function decryptData(sharedSecret, encryptedDataWithIvBase64) {
  const encryptedDataWithIv = await base64ToArrayBuffer(encryptedDataWithIvBase64);

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

// 辅助函数：将 ArrayBuffer 转换为 Base64 字符串
function arrayBufferToBase64(buffer) {
  const binary = String.fromCharCode.apply(null, new Uint8Array(buffer));
  return btoa(binary);
}
