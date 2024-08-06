// 导入 request.js 请求工具
import request from '@/utils/request.js';

// 提供调用注册接口的函数
export const userRegisterService = (registerData) => {
  return request.post('/user/register', registerData); // 直接传递对象，axios 会自动处理 JSON 格式
}

// 提供调用登录接口的函数
export const userLoginService = (loginData) => {
  return request.post('/user/login', loginData); // 直接传递对象，axios 会自动处理 JSON 格式
}