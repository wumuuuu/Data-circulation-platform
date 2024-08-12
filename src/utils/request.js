// 定义一个变量，记录公共的前缀 baseURL
const baseURL = '/api';

// 包装 Fetch API 的请求函数
const request = async (url, options = {}) => {
  // 设置默认的请求头
  options.headers = {
    'Content-Type': 'application/json',
    ...options.headers
  };

  // 拼接完整的 URL
  const fullUrl = baseURL + url;

  try {
    const response = await fetch(fullUrl, options);

    // 检查响应状态
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    // 解析响应数据
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Fetch error:', error);
    throw error;
  }
};

// 定义 POST 请求函数
const post = (url, data) => {
  return request(url, {
    method: 'POST',
    body: JSON.stringify(data)
  });
};

// 定义 GET 请求函数
const get = (url) => {
  return request(url, {
    method: 'GET'
  });
};

// 导出请求函数
export default {
  post,
  get,
};
