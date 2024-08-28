/**
 * 生成数据传输的密钥对
 * 不要和签名密钥对搞混
 */

// 用于存储共享密钥的变量
let sharedKey = null;

// 设置共享密钥并存储在 sessionStorage 中
export async function setSharedKey(key) {
  sharedKey = key;
  // 将共享密钥导出为 JWK 格式并存储在 sessionStorage 中
  await window.crypto.subtle.exportKey('jwk', key).then(jwk => {
    sessionStorage.setItem('sharedKey', JSON.stringify(jwk));
  });
}

// 从 sessionStorage 中获取共享密钥并重新导入
export async function getSharedKey() {
  if (sharedKey) {
    return sharedKey;
  }

  const jwk = sessionStorage.getItem('sharedKey');
  if (jwk) {
    sharedKey = await window.crypto.subtle.importKey(
      'jwk',
      JSON.parse(jwk),
      { name: 'AES-GCM', length: 256 },
      true,
      ['encrypt', 'decrypt']
    );
  }

  return sharedKey;
}

// 清除共享密钥
export function clearSharedKey() {
  sharedKey = null;
  sessionStorage.removeItem('sharedKey');
}


// 生成 ECDH 密钥对
export async function generateECDHKeyPair() {
  return await window.crypto.subtle.generateKey({
    name: "ECDH",
    namedCurve: "P-256" // 使用 P-256 椭圆曲线
  }, true, ["deriveKey", "deriveBits"]);
}

// 生成共享密钥并存储
export async function generateSharedECDHSecret(clientPrivateKey, serverPublicKeyBase64) {
  // 等待 base64ToArrayBuffer 完成并获取 ArrayBuffer
  const serverPublicKeyArrayBuffer = await base64ToArrayBuffer(serverPublicKeyBase64);

  const serverPublicKey = await window.crypto.subtle.importKey(
    "spki",
    serverPublicKeyArrayBuffer,
    { name: "ECDH", namedCurve: "P-256" },
    true,
    []
  );

  const sharedSecret = await window.crypto.subtle.deriveKey(
    {
      name: "ECDH",
      public: serverPublicKey
    },
    clientPrivateKey,
    {
      name: "AES-GCM",
      length: 256
    },
    true,
    ["encrypt", "decrypt"]
  );

  const exportedSharedKey = await window.crypto.subtle.exportKey('raw', sharedSecret);
  console.log("Shared Secret (Hex):", Array.from(new Uint8Array(exportedSharedKey)).map(b => b.toString(16).padStart(2, '0')).join(''));

  // 在这里设置共享密钥
  await setSharedKey(sharedSecret);
}

// 使用共享密钥加密数据
export async function encryptData(sharedSecret, data) {
  const iv = window.crypto.getRandomValues(new Uint8Array(12)); // 生成随机 IV

  const encryptedData = await window.crypto.subtle.encrypt({
    name: "AES-GCM",
    iv: iv
  }, sharedSecret, new TextEncoder().encode(data));

  // 将 IV 和加密数据合并为一个 Uint8Array
  const encryptedDataWithIv = new Uint8Array(iv.length + encryptedData.byteLength);
  encryptedDataWithIv.set(iv);
  encryptedDataWithIv.set(new Uint8Array(encryptedData), iv.length);

  // 返回 Base64 编码的字符串
  return arrayBufferToBase64(encryptedDataWithIv.buffer);
}




// 使用共享密钥解密数据
export async function decryptData(sharedSecret, encryptedDataWithIvBase64) {
  // 使用 await 等待异步操作完成
  const encryptedDataWithIv = await base64ToArrayBuffer(encryptedDataWithIvBase64);

  // 将 ArrayBuffer 转换为 Uint8Array 以便使用 slice 方法
  const encryptedDataArray = new Uint8Array(encryptedDataWithIv);

  const iv = encryptedDataArray.slice(0, 12); // 提取前 12 字节为 IV
  const encryptedData = encryptedDataArray.slice(12); // 剩余部分为加密数据

  const decryptedData = await window.crypto.subtle.decrypt({
    name: "AES-GCM",
    iv: iv
  }, sharedSecret, encryptedData);

  return new TextDecoder().decode(decryptedData);
}



// 辅助函数：将 Base64 字符串转换为 ArrayBuffer
export async function base64ToArrayBuffer(base64) {
  const binaryString = window.atob(base64);
  const len = binaryString.length;
  const bytes = new Uint8Array(len);
  for (let i = 0; i < len; i++) {
    bytes[i] = binaryString.charCodeAt(i);
  }
  return bytes.buffer;
}

// 辅助函数：将 ArrayBuffer 转换为 Base64 字符串
export async function arrayBufferToBase64(buffer) {
  const binary = String.fromCharCode.apply(null, new Uint8Array(buffer));
  return window.btoa(binary);
}
