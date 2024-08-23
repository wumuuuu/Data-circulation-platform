import { encryptData, decryptData, sharedKey } from '@/utils/DH.js';

const baseURL = '/api';

// 包装 Fetch API 的请求函数
const request = async (url, options = {}) => {
  options.headers = {
    'Content-Type': 'application/json',
    ...options.headers
  };

  const fullUrl = baseURL + url;

  try {
    // 处理加密
    if (options.body && sharedKey && !url.includes('/sendPublicKey') && !url.includes('/getServerPublicKey')) {
      const encryptedBody = await encryptData(options.body); // 这里处理对象
      options.body = JSON.stringify(encryptedBody); // 在这里进行字符串化
    } else {
      options.body = JSON.stringify(options.body); // 直接字符串化对象
    }

    const response = await fetch(fullUrl, options);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const encryptedResponse = await response.json();
    const data = (sharedKey && !url.includes('/sendPublicKey') && !url.includes('/getServerPublicKey'))
      ? await decryptData(encryptedResponse)
      : encryptedResponse;

    return data;
  } catch (error) {
    console.error('Fetch error:', error);
    throw error;
  }
};

const post = (url, data) => {
  return request(url, {
    method: 'POST',
    body: data // 不需要在这里进行 JSON.stringify
  });
};

const get = (url) => {
  return request(url, {
    method: 'GET'
  });
};

export default {
  post,
  get,
};