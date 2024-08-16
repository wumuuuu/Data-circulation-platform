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

// 提供调用检查用户名是否存在的接口的函数
export const checkUserExists = async (username) => {
  try {
    const response = await request.get(`/user/exists?username=${encodeURIComponent(username)}`);
    console.log('Check user exists response:', response);
    return response;
  } catch (error) {
    console.error('Check user exists error:', error);
    throw error;
  }
};

// 提供调用申请提交接口的函数
export const submitApplication = async (applicationData) => {
  try {
    const response = await request.post('/application/submit', applicationData);
    console.log('Application submit response:', response);
    return response;
  } catch (error) {
    console.error('Application submit error:', error);
    throw error;
  }
};

// 调用后端同意/拒绝申请接口的函数
export const Application = async (application) => {
  return request.post(`/application/updateApplication`, application);
};

// 调用后端获取所有申请记录
export const getApplications = async () => {
  return request.get('/application/pending');
};

// 调用后端获取用户申请状态的函数
export const getApplicationStatus = async (username) => {
  try {
    const response = await request.get(`/application/status?username=${username}`);
    console.log('Get application status response:', response);
    return response;
  } catch (error) {
    console.error('Get application status error:', error);
    throw error;
  }
};

// 获取历史签名者的函数
export const getHistoricalSigners = async (dataId) => {
  try {
    const response = await request.get(`/process/getSigners?data_id=${dataId}`);
    console.log('Historical signers response:', response);
    return response;
  } catch (error) {
    console.error('Error fetching historical signers:', error);
    throw error;
  }
};