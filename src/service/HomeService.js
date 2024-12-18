import { get, post } from '@/utils/request.js'
import { ElMessage } from 'element-plus'

export const fetchDataRecord = async () => {
  try {
    // 发起请求下载文件
    const response = await get(`/task/getCompletedData`);
    return response.data;
  } catch (error) {
    console.error('Error:', error);
  }
};

export async function searchUsernamesAPI(username) {
  const response = await get(`/user/search?username=${encodeURIComponent(username)}`);
  if (response.success) {
    return response;
  }
}