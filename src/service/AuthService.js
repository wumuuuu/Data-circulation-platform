// 导入所需的加密和请求处理工具函数
import {
  clearSharedKey, // 用于清除存储的共享密钥
  initKeyExchange, // 用于初始化密钥交换过程
  clientKeyPair, // 存储客户端的密钥对
  sharedKey // 存储生成的共享密钥
} from '@/utils/cryptoUtils.js'

import {
  generatePrivateKey, // 使用 Web Worker 生成私钥的函数
  calculatePublicKey, // 使用 Web Worker 计算公钥的函数
  encryptData, // 使用 Web Worker 加密数据的函数
  decryptData // 使用 Web Worker 解密数据的函数
} from '@/service/cryptoWorkerService.js';

import { post } from '@/utils/request.js'; // 导入用于发送 POST 请求的函数
import router from '@/router.js'; // 导入 Vue 路由器，用于页面跳转
import { ElMessage } from 'element-plus'

// 处理用户注册请求的函数
export async function onRegister(registerData) {
  // 验证用户输入的两次密码是否一致
  if (registerData.password !== registerData.rePassword) {
    alert('两次输入的密码不一致');
    return; // 如果密码不一致，则停止注册流程
  }

  // 如果客户端密钥对未生成，则先初始化密钥交换
  if (!clientKeyPair) {
    await initKeyExchange();
  }

  // 发送请求到服务器，检查用户名是否已存在
  const response = await post('/find-username', { username: registerData.username });

  // 如果用户名已存在，则停止注册流程
  if (!response.success) {
    ElMessage.error('用户名已存在');
    return;
  }

  try {
    // 使用 Web Worker 生成私钥
    const privateKey = await generatePrivateKey();



    // 使用生成的私钥计算相应的公钥
    const publicKey = await calculatePublicKey(privateKey);



    // 使用生成的共享密钥加密用户的密码和公钥
    const encryptedPassword = await encryptData(sharedKey, stringToArrayBuffer(registerData.password));
    const encryptedPublicKey = await encryptData(sharedKey, stringToArrayBuffer(publicKey));


    // 解密加密后的密码以验证加密过程是否正确
    const decryptedPassword = await decryptData(sharedKey, encryptedPassword);
    console.log("Decrypted Password:", decryptedPassword);


    // 复制注册数据对象，并用加密后的密码替换原始密码
    const payload = {
      ...registerData,
      password: encryptedPassword, // 加密后的密码
      public_key: encryptedPublicKey, // 加密后的公钥
      rePassword: '' // 清空重复密码字段
    };

    // 验证解密后的密码是否与用户输入的密码一致
    if (decryptedPassword === registerData.password) {
      console.log("解密验证成功，数据一致。");
    } else {
      console.error("解密验证失败，数据不一致！");
      return;
    }

    // 发送注册请求到服务器
    const registerResponse = await post('/register', payload);

    // 如果注册成功，执行以下操作
    if (registerResponse.success) {
      ElMessage.success('注册成功');
      clearSharedKey(); // 清除存储的共享密钥
      localStorage.setItem('privateKey', privateKey.toString()); // 将私钥保存到本地存储
      document.getElementById('savePrivateKeyButton').style.display = 'block'; // 显示保存私钥的按钮
    } else {
      // 如果注册失败，输出错误信息
      ElMessage.error('注册失败');
      console.error('注册失败', registerResponse.message);
    }
  } catch (error) {
    // 捕获并处理注册过程中的任何错误
    console.error('注册过程中发生错误:', error);
  }
}

// 处理用户登录请求的函数
export async function onLogin(loginData) {
  // 如果客户端密钥对未生成，则先初始化密钥交换
  if (!clientKeyPair) {
    await initKeyExchange();
  }

  try {

    const encodedData = new TextEncoder().encode(loginData.password); // data 是字符串

    // 使用共享密钥加密用户输入的密码
    const encryptedPassword = await encryptData(sharedKey, encodedData);

    console.log("Encrypted Password:", encryptedPassword);

    // 解密加密后的密码以验证加密过程是否正确
    const decryptedPassword = await decryptData(sharedKey, encryptedPassword);
    console.log("Decrypted Password:", decryptedPassword);

    // 发送登录请求到服务器，传递加密后的用户名和密码
    const response = await post('/login', {
      username: loginData.username,
      password: encryptedPassword
    });

    // 如果登录成功，执行以下操作
    if (response.success) {
      ElMessage.success('登录成功');

      // 保存服务器返回的身份标识（如角色和 Token）到本地存储
      const role = response.data;
      localStorage.setItem('role', role);
      localStorage.setItem('authToken', response.token);
      localStorage.setItem('username', loginData.username);

      // 根据用户角色跳转到相应的页面
      if (role === 'Admin') {
        await router.push({ name: 'UserMgr' });
      } else {
        await router.push({ name: 'UserMgr' });
      }
    } else {
      // 如果登录失败，输出错误信息
      ElMessage.error('登录失败');
      console.error('登录失败', response.message);
    }
  } catch (error) {
    // 捕获并处理登录过程中的任何错误
    console.error('登录过程中发生错误:', error);
  }
}

function stringToArrayBuffer(str) {
  const encoder = new TextEncoder();
  return encoder.encode(str);  // 返回 Uint8Array (ArrayBufferView)
}



// 发送加密数据到服务器的函数
export async function sendEncryptedData(data) {
  // 检查共享密钥是否已初始化
  if (!sharedKey) {
    throw new Error('共享密钥未初始化');
  }

  try {
    // 使用共享密钥加密数据
    const encryptedData = await encryptData(sharedKey, data);

    // 发送加密数据到服务器的安全端点
    const response = await post('/secure-endpoint', { encryptedData });

    // 如果服务器成功响应，解密服务器返回的数据
    if (response.success) {
      const decryptedResponse = await decryptData(sharedKey, response.encryptedData);
      console.log('服务器响应:', decryptedResponse);
    } else {
      // 如果发送数据失败，输出错误信息
      console.error('发送加密数据失败', response.message);
    }
  } catch (error) {
    // 捕获并处理加密数据传输过程中的任何错误
    console.error('加密数据传输失败:', error);
  }
}

// 将私钥保存到指定位置的函数
export async function toSavePrivateKey() {
  // 从本地存储中获取私钥
  const privateKey = localStorage.getItem('privateKey');

  // 设置文件保存对话框的选项
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

    // 将私钥写入指定位置的文件中
    await writable.write(privateKey);
    await writable.close();
    alert('私钥保存成功');

    // 保存后，移除本地存储中的私钥
    localStorage.removeItem('privateKey');
    document.getElementById('savePrivateKeyButton').style.display = 'none'; // 隐藏保存私钥的按钮
  } catch (error) {
    // 捕获并处理保存私钥过程中的任何错误
    alert('私钥保存失败');
    console.error('保存失败', error);
  }
}
