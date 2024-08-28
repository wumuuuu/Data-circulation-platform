import {
  arrayBufferToBase64,
  generateECDHKeyPair,
  encryptData,
  decryptData,
  getSharedKey,
  generateSharedECDHSecret, clearSharedKey
} from '@/utils/cryptoUtils.js'

import { post } from '@/utils/request.js';

let clientKeyPair = null;
let sharedSecret = null;
let serverPublicKey = null;

// 初始化 ECDH 密钥对, 获取服务器公钥, 生成共享密钥
export async function initKeyExchange() {
  clientKeyPair = await generateECDHKeyPair();
  serverPublicKey = await exchangeKeys(clientKeyPair.publicKey);
  await generateSharedECDHSecret(clientKeyPair.privateKey, serverPublicKey);
  sharedSecret = await getSharedKey();
}

export async function clearKeyExchange() {
  clientKeyPair = null;
  serverPublicKey = null;
  sharedSecret = null;
}

// 处理注册请求
export async function onRegister(registerData) {
  if (!clientKeyPair) {
    await initKeyExchange();
  }

  // 使用生成的共享密钥加密密码
  const encryptedPassword = await encryptData(sharedSecret, registerData.password);

  // 解密数据进行验证
  const decryptedPassword = await decryptData(sharedSecret, encryptedPassword);
  console.log("Decrypted Password:", decryptedPassword);

  // 复制原始 registerData，并将密码替换为加密后的密码
  const payload = {
    ...registerData,
    password: encryptedPassword,
    rePassword: ''
  };

  if (decryptedPassword === registerData.password) {
    console.log("解密验证成功，数据一致。");
  } else {
    console.error("解密验证失败，数据不一致！");
  }

  // 发送注册请求
  const response = await post('/register', payload);

  if (response.success) {
    console.log('注册成功');
    clearSharedKey();
    await clearKeyExchange();
  } else {
    console.error('注册失败', response.message);
  }


}

// 处理登录请求
export async function onLogin(loginData) {
  if (!clientKeyPair) {
    await initKeyExchange();
  }

  // 使用生成的共享密钥加密密码
  const encryptedPassword = await encryptData(sharedSecret, loginData.password);
  console.log("Decrypted Password:", encryptedPassword);

  // 解密数据进行验证
  const decryptedPassword = await decryptData(sharedSecret, encryptedPassword);
  console.log("Decrypted Password:", decryptedPassword);

  // 发送登录请求
  const response = await post('/login', {
    username: loginData.username,
    password: encryptedPassword
  });

  if (response.success) {
    console.log('登录成功');
    // 保存 Token 或其他身份标识
    localStorage.setItem('authToken', response.token);
  } else {
    console.error('登录失败', response.message);
  }
}

// 交换密钥：发送客户端公钥到服务器，接收服务器的公钥
async function exchangeKeys(publicKey) {
  const exportedPublicKey = await window.crypto.subtle.exportKey('spki', publicKey);
  const publicKeyBase64 = await arrayBufferToBase64(exportedPublicKey);

  const response = await post('/exchange-keys', { clientPublicKey: publicKeyBase64 });

  if (response.success) {
    return response.data;
  } else {
    throw new Error('密钥交换失败');
  }
}

// 发送加密数据
export async function sendEncryptedData(data) {
  if (!sharedSecret) {
    throw new Error('共享密钥未初始化');
  }

  const encryptedData = await encryptData(sharedSecret, data);

  const response = await post('/api/secure-endpoint', { encryptedData });

  if (response.success) {
    const decryptedResponse = await decryptData(sharedSecret, response.encryptedData);
    console.log('服务器响应:', decryptedResponse);
  } else {
    console.error('发送加密数据失败', response.message);
  }
}
