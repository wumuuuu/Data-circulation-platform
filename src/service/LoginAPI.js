import { userLoginService, userRegisterService } from '@/api/api.js'
import { calculatePublicKey, generatePrivateKey } from '@/utils/CountKey.js'
import router from '@/router.js'

export const onLogin = async (loginData) => {
  try {
    // 调用接口完成登录
    const result = await userLoginService(loginData.value);

    // 根据返回的结果代码进行处理
    switch (result.code) {
      case 0:
        // 登录成功，保存 token，并重定向到 Home 页面
        localStorage.setItem('userToken', result.token || 'dummy-token'); // 使用返回的 token 或默认值
        localStorage.setItem('username', result.data);
        alert(result.msg || '登录成功');
        await router.push({ name: 'Home' });
        break;

      case 1:
        // 用户不存在
        alert(result.msg || '用户不存在');
        break;

      default:
        // 密码错误或其他情况
        alert(result.msg || '密码错误');
        break;
    }
  } catch (error) {
    // 捕获并处理登录过程中的异常
    console.error('登录异常:', error);
    alert('登录异常');
  }
}

export const onRegister = async (registerData) => {
  try {
    // 验证密码是否一致
    if (registerData.value.password !== registerData.value.rePassword) {
      alert('两次输入的密码不一致');
      return; // 阻止继续执行注册
    }
    // 生成私钥
    const privateKey = generatePrivateKey();
    // 生成公钥
    registerData.value.public_key = calculatePublicKey(privateKey);

    // 调用后台接口进行注册
    let result = await userRegisterService(registerData.value);
    if (result.code === 200) {
      alert(result.msg || '注册成功');
      localStorage.setItem('privateKey', privateKey);
      document.getElementById('savePrivateKeyButton').style.display = 'block';
      return 1;
    } else {
      alert(result.msg || '注册失败');
    }
  } catch (error) {
    alert('注册异常');
  }
  return 0;
}