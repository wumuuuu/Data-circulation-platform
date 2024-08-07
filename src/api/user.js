// 导入 request.js 请求工具
import request from '@/utils/request.js';

// 提供调用注册接口的函数
export const userRegisterService = async (registerData) => {
  console.log(registerData);
  try {
    const response = await request.post('/user/register', registerData);
    console.log('Register response:', response);
    return response;
  } catch (error) {
    console.error('Register error:', error);
    throw error;
  }
};

// 提供调用登录接口的函数
export const userLoginService = async (loginData) => {
  try {
    const response = await request.post('/user/login', loginData);
    console.log('Login response:', response);
    return response;
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};
