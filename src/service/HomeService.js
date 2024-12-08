import { get } from '@/utils/request.js'
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