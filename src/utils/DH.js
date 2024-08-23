import request from '@/utils/request.js'; // 引入封装的 request 模块

export let sharedKey; // 全局变量，用于存储生成的共享密钥

/**
 * 初始化 Diffie-Hellman 密钥交换过程
 * 1. 生成前端密钥对
 * 2. 将前端公钥发送给服务器
 * 3. 获取服务器公钥
 * 4. 生成共享密钥
 */
export async function initializeDH() {
  try {
    // 1. 前端生成密钥对
    const keyPair = await generateKeyPair();

    // 2. 导出前端的公钥并发送给后端
    const publicKey = await exportPublicKey(keyPair);
    await sendPublicKeyToServer(publicKey);

    // 3. 接收后端的公钥
    const serverPublicKeyData = await getServerPublicKey();
    const serverPublicKey = await importServerPublicKey(serverPublicKeyData);

    // 4. 生成共享密钥
    sharedKey = await deriveSharedKey(keyPair, serverPublicKey);

    // console.log('共享密钥已生成，所有通信将使用该密钥加密:', sharedKey);
  } catch (error) {
    console.error('初始化 Diffie-Hellman 过程时出错:', error);
  }
}

/**
 * 生成 ECDH 密钥对
 * @returns {Promise<CryptoKeyPair>} 返回生成的密钥对 (publicKey, privateKey)
 */
export async function generateKeyPair() {
  const keyPair = await window.crypto.subtle.generateKey(
    {
      name: "ECDH", // 使用椭圆曲线 Diffie-Hellman 算法
      namedCurve: "P-256", // 使用 P-256 椭圆曲线
    },
    true, // 是否可以导出生成的密钥
    ["deriveKey"] // 允许使用密钥派生共享密钥
  );
  return keyPair;
}

/**
 * 导出公钥为 SPKI 格式
 * @param {CryptoKeyPair} keyPair - 密钥对对象
 * @returns {Promise<ArrayBuffer>} 返回导出的公钥数据
 */
export async function exportPublicKey(keyPair) {
  return await window.crypto.subtle.exportKey("spki", keyPair.publicKey);
}

/**
 * 导入服务器的公钥
 * @param {ArrayBuffer} serverPublicKeyData - 服务器发送的公钥数据
 * @returns {Promise<CryptoKey>} 返回导入的公钥对象
 */
export async function importServerPublicKey(serverPublicKeyData) {
  return await window.crypto.subtle.importKey(
    "spki",
    serverPublicKeyData,
    {
      name: "ECDH",
      namedCurve: "P-256",
    },
    true,
    []
  );
}

/**
 * 使用本地私钥和服务器公钥生成共享密钥
 * @param {CryptoKeyPair} keyPair - 本地密钥对
 * @param {CryptoKey} serverPublicKey - 服务器的公钥
 * @returns {Promise<CryptoKey>} 返回生成的共享密钥
 */
export async function deriveSharedKey(keyPair, serverPublicKey) {
  sharedKey = await window.crypto.subtle.deriveKey(
    {
      name: "ECDH",
      public: serverPublicKey,
    },
    keyPair.privateKey,
    { name: "AES-GCM", length: 256 }, // 派生 AES-GCM 密钥
    true,
    ["encrypt", "decrypt"]
  );

  console.log("Shared Key 已生成:", sharedKey);
  return sharedKey;
}
/**
 * 将公钥发送到服务器
 * @param {ArrayBuffer} publicKey - 要发送的公钥
 */
export async function sendPublicKeyToServer(publicKey) {
  try {
    const publicKeyBase64 = btoa(String.fromCharCode(...new Uint8Array(publicKey)));

    const response = await request.post('/DH/sendPublicKey', {
      publicKey: publicKeyBase64
    });

    if (response.code === 200) {
      console.log('Public key successfully sent to server');
    } else {
      console.error(`Error: ${response.msg}, Code: ${response.code}`);
      throw new Error('Failed to send public key to server');
    }
  } catch (error) {
    console.error('Error sending public key to server:', error);
  }
}





/**
 * 从服务器获取公钥
 * @returns {Promise<ArrayBuffer>} 返回从服务器获取的公钥数据
 */
export async function getServerPublicKey() {
  const response = await request.get('/DH/getServerPublicKey');
  console.log('Get server public key:', response);
  if (!response) {
    throw new Error('Failed to get server public key');
  }

  const serverPublicKey = Uint8Array.from(atob(response.publicKey), c => c.charCodeAt(0));
  return serverPublicKey.buffer;
}

/**
 * 使用共享密钥加密数据
 * @param {Object} data - 要加密的原始数据
 * @returns {Promise<Object>} 返回加密后的数据和 IV
 */
export async function encryptData(data) {
  if (!sharedKey || !(sharedKey instanceof CryptoKey)) {
    throw new Error('Shared key is not initialized or is not of type CryptoKey');
  }

  console.log("Using Shared Key for encryption:", sharedKey);

  const iv = window.crypto.getRandomValues(new Uint8Array(12)); // 生成随机 IV
  const encodedData = new TextEncoder().encode(JSON.stringify(data)); // 将数据编码为 Uint8Array

  const encryptedData = await window.crypto.subtle.encrypt(
    {
      name: 'AES-GCM',
      iv: iv,
    },
    sharedKey,
    encodedData
  );

  return {
    iv: btoa(String.fromCharCode(...iv)), // 返回 IV 的 Base64 字符串
    data: btoa(String.fromCharCode(...new Uint8Array(encryptedData))), // 返回加密数据的 Base64 字符串
  };
}

/**
 * 使用共享密钥解密数据
 * @param {Object} encryptedResponse - 包含加密数据和 IV 的对象
 * @returns {Promise<Object>} 返回解密后的数据
 */
export async function decryptData(encryptedResponse) {
  if (!sharedKey || !(sharedKey instanceof CryptoKey)) {
    throw new Error('Shared key is not initialized');
  }

  const iv = Uint8Array.from(atob(encryptedResponse.iv), c => c.charCodeAt(0)); // 解码 IV
  const encryptedData = Uint8Array.from(atob(encryptedResponse.data), c => c.charCodeAt(0)); // 解码加密数据

  const decryptedData = await window.crypto.subtle.decrypt(
    {
      name: 'AES-GCM',
      iv: iv,
    },
    sharedKey,
    encryptedData
  );

  const decodedData = new TextDecoder().decode(decryptedData); // 解码为字符串
  return JSON.parse(decodedData); // 转换为 JSON 对象
}
