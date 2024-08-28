// 导入所需的加密和请求处理工具函数
import {
  arrayBufferToBase64, // 将 ArrayBuffer 转换为 Base64 字符串
  generateECDHKeyPair, // 生成 ECDH 密钥对（用于非对称加密）
  encryptData, // 使用对称加密共享密钥加密数据
  decryptData, // 使用对称加密共享密钥解密数据
  getSharedKey, // 获取已生成的共享密钥
  generateSharedECDHSecret, // 生成 ECDH 的共享密钥
  clearSharedKey // 清除共享密钥
} from '@/utils/cryptoUtils.js';
import { post } from '@/utils/request.js'; // 导入 POST 请求函数
import { calculatePublicKey, generatePrivateKey } from '@/utils/CountKey.js';
import router from '@/router.js'

// 初始化全局变量，用于存储密钥对和共享密钥
let clientKeyPair = null;
let sharedSecret = null;
let serverPublicKey = null;

// 初始化 ECDH 密钥对, 获取服务器公钥, 生成共享密钥
export async function initKeyExchange() {
  // 生成客户端的 ECDH 密钥对
  clientKeyPair = await generateECDHKeyPair();

  // 将客户端的公钥发送到服务器，接收服务器返回的公钥
  serverPublicKey = await exchangeKeys(clientKeyPair.publicKey);

  // 使用客户端私钥和服务器公钥生成共享密钥
  await generateSharedECDHSecret(clientKeyPair.privateKey, serverPublicKey);

  // 获取生成的共享密钥
  sharedSecret = await getSharedKey();
}

// 清除密钥交换信息
export async function clearKeyExchange() {
  clientKeyPair = null; // 清空客户端密钥对
  serverPublicKey = null; // 清空服务器公钥
  sharedSecret = null; // 清空共享密钥
}

// 处理注册请求
export async function onRegister(registerData) {
  // 验证两次输入的密码是否一致
  if (registerData.value.password !== registerData.value.rePassword) {
    alert('两次输入的密码不一致');
    return; // 阻止继续执行注册流程
  }

  // 如果密钥对未生成，先初始化密钥交换
  if (!clientKeyPair) {
    await initKeyExchange();
  }

  // 查找用户名是否重复
  const Response = await post('/find-username', {
    username: registerData.username
  });

  if (Response.success) {
    console.log('用户名没有重复');
  } else {
    console.error('用户名已存在', Response.message);
    return; // 阻止注册，因为用户名已存在
  }

  // 生成签名私钥（一般用于数字签名）
  const privateKey = generatePrivateKey();

  // 计算签名公钥
  const publicKey = calculatePublicKey(privateKey);

  // 使用生成的共享密钥加密用户的密码和公钥
  const encryptedPassword = await encryptData(sharedSecret, registerData.password);
  const encryptedPublicKey = await encryptData(sharedSecret, publicKey);

  // 解密数据以进行验证，确保加密过程正确
  const decryptedPassword = await decryptData(sharedSecret, encryptedPassword);
  console.log("Decrypted Password:", decryptedPassword);

  // 复制原始的注册数据，并将密码替换为加密后的密码
  const payload = {
    ...registerData,
    password: encryptedPassword,
    public_key: encryptedPublicKey, // 包含加密后的公钥
    rePassword: '' // 清空重复密码字段
  };

  // 验证解密后的密码是否与原密码一致
  if (decryptedPassword === registerData.password) {
    console.log("解密验证成功，数据一致。");
  } else {
    console.error("解密验证失败，数据不一致！");
  }

  // 发送注册请求到服务器
  const response = await post('/register', payload);

  if (response.success) {
    console.log('注册成功');
    clearSharedKey(); // 清除共享密钥
    await clearKeyExchange(); // 清除密钥交换信息
    localStorage.setItem('privateKey', privateKey.toString()); // 将私钥保存到本地存储
    document.getElementById('savePrivateKeyButton').style.display = 'block'; // 显示保存私钥的按钮
  } else {
    console.error('注册失败', response.message);
  }
}

// 处理登录请求
export async function onLogin(loginData) {
  // 如果密钥对未生成，先初始化密钥交换
  if (!clientKeyPair) {
    await initKeyExchange();
  }

  // 使用共享密钥加密用户输入的密码
  const encryptedPassword = await encryptData(sharedSecret, loginData.password);
  console.log("Decrypted Password:", encryptedPassword);

  // 解密数据以进行验证，确保加密过程正确
  const decryptedPassword = await decryptData(sharedSecret, encryptedPassword);
  console.log("Decrypted Password:", decryptedPassword);

  // 发送登录请求到服务器
  const response = await post('/login', {
    username: loginData.username,
    password: encryptedPassword
  });

  if (response.success) {
    console.log('登录成功');

    // 将服务器返回的身份标识（如 Token）保存到本地存储
    const role = response.data;
    localStorage.setItem('role', role);
    localStorage.setItem('authToken', response.token);
    localStorage.setItem('username', loginData.username);

    // 根据角色跳转到相应的页面
    if (role === 'Admin') {
      await router.push({ name: 'UserMgr' });
    } else {
      await router.push({ name: 'Home' });
    }
  } else {
    console.error('登录失败', response.message);
  }
}

// 交换密钥：发送客户端公钥到服务器，接收服务器的公钥
async function exchangeKeys(publicKey) {
  // 将客户端的公钥导出为 SPKI 格式的 ArrayBuffer
  const exportedPublicKey = await window.crypto.subtle.exportKey('spki', publicKey);

  // 将 ArrayBuffer 转换为 Base64 字符串
  const publicKeyBase64 = await arrayBufferToBase64(exportedPublicKey);

  // 发送客户端公钥到服务器，等待接收服务器返回的公钥
  const response = await post('/exchange-keys', { clientPublicKey: publicKeyBase64 });

  if (response.success) {
    return response.data; // 返回服务器的公钥
  } else {
    throw new Error('密钥交换失败');
  }
}

// 发送加密数据到服务器
export async function sendEncryptedData(data) {
  if (!sharedSecret) {
    throw new Error('共享密钥未初始化');
  }

  // 使用共享密钥加密数据
  const encryptedData = await encryptData(sharedSecret, data);

  // 发送加密数据到服务器的安全端点
  const response = await post('/api/secure-endpoint', { encryptedData });

  if (response.success) {
    // 解密服务器响应的数据
    const decryptedResponse = await decryptData(sharedSecret, response.encryptedData);
    console.log('服务器响应:', decryptedResponse);
  } else {
    console.error('发送加密数据失败', response.message);
  }
}

// 保存私钥到指定位置
export async function toSavePrivateKey() {
  const privateKey = localStorage.getItem('privateKey');
  const options = {
    types: [
      {
        description: 'Text Files',
        accept: {
          'text/plain': ['.pem'],
        },
      },
    ],
  };

  try {
    // 显示文件保存对话框并获取文件句柄
    const handle = await window.showSaveFilePicker(options);
    const writable = await handle.createWritable();

    // 写入私钥到指定位置
    await writable.write(privateKey);
    await writable.close();
    alert('私钥保存成功');

    // 移除本地存储中的私钥
    localStorage.removeItem('privateKey');
    document.getElementById('savePrivateKeyButton').style.display = 'none'; // 隐藏保存私钥的按钮
  } catch (error) {
    console.error('保存失败', error);
  }
}
