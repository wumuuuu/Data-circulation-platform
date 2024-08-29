import { sharedKey } from '@/utils/cryptoUtils.js'

const baseURL = '/api';

// 判断是否需要加密或解密的辅助函数
const isEncryptionRequired = (url) => {
  return !url.includes('/exchange-keys') && !url.includes('/find-username');
};

// 包装 Fetch API 的请求函数
const request = async (url, options = {}) => {
  options.headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };
  // 检查并确保 options.body 是对象
  if (options.body && typeof options.body === 'object') {
    // 直接字符串化加密后的请求体或普通对象
    options.body = JSON.stringify(options.body);
    console.log("Request body:", options.body);
  }

  // 打印最终的请求选项
  console.log("Final request options:", options);

  try {
    const response = await fetch(baseURL + url, options);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const apiResponse = await response.json();

    const decryptedData = isEncryptionRequired(url) && apiResponse.encryptedData
      ? await decryptData(sharedKey, apiResponse.encryptedData)
      : apiResponse.data;

    const data = typeof decryptedData === 'string' && decryptedData.startsWith('{')
      ? JSON.parse(decryptedData)
      : decryptedData;

    console.log("data  = " + data);

    if (apiResponse.code === 200) {
      return {
        success: apiResponse.code === 200,
        data: data,
        message: apiResponse.message,
      };
    } else {
      console.error(`Error ${apiResponse.code}: ${apiResponse.message}`);
      return {
        success: apiResponse.code === 200,
        data: data,
        message: apiResponse.message,
      };
      throw new Error(apiResponse.message);
    }
  } catch (error) {
    console.error('Fetch error:', error);
    throw error;
  }
};




// POST 请求封装
export const post = (url, data) => {
  return request(url, {
    method: 'POST',
    body: data // 不需要在这里进行 JSON.stringify
  });
};

// GET 请求封装
export const get = (url) => {
  return request(url, {
    method: 'GET'
  });
};

export default {
  post,
  get,
};
