// 创建一个新的 Web Worker 实例，指向包含加密逻辑的文件 'cryptoWorker.js'
const cryptoWorker = new Worker(new URL('@/utils/cryptoWorker.js', import.meta.url));

// 发送消息到 Web Worker 并返回 Promise
function sendMessageToWorker(message) {
  return new Promise((resolve, reject) => {
    cryptoWorker.onmessage = function (e) {
      const { type, result } = e.data;
      if (type === 'error') {
        reject(result);
      } else {
        resolve(result);
      }
    };
    cryptoWorker.postMessage(message);
  });
}

// 函数：向 Web Worker 发送生成私钥的请求，并返回生成的私钥
export function generatePrivateKey() {
  return sendMessageToWorker({ type: 'generatePrivateKey' });
}

// 函数：向 Web Worker 发送计算公钥的请求，并返回计算的公钥
export function calculatePublicKey(privateKey) {
  return sendMessageToWorker({ type: 'calculatePublicKey', payload: { privateKey } });
}

// 函数：向 Web Worker 发送加密数据的请求，并返回加密后的数据
export function encryptData(sharedSecret, data) {
  return sendMessageToWorker({ type: 'encryptData', payload: { sharedSecret, data } });
}

// 函数：向 Web Worker 发送解密数据的请求，并返回解密后的数据
export function decryptData(sharedSecret, encryptedData) {
  return sendMessageToWorker({ type: 'decryptData', payload: { sharedSecret, encryptedData } });
}
