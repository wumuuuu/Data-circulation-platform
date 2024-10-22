// ApplicationService.js

import { get, post } from '@/utils/request.js'
import { ElMessage } from 'element-plus'


const username = localStorage.getItem('username');
/**
 * 获取所有数据所有方的名字列表
 * @returns {Promise<Array<string>>} 包含所有数据所有方名字的数组
 */
export async function fetchDataOwners() {
  try {
    const response = await get(`/data-owners`);

    if (response.success) {
      return response.data;
    } else {
      throw new Error(response.message || 'Failed to fetch data owners');
    }
  } catch (error) {
    console.error('Error fetching data owners:', error);
    throw error;
  }
}

/**
 * 获取指定用户的所有申请记录
 * @returns {Promise<Array<Object>>} 返回包含该用户所有申请记录的数组
 */
export async function fetchApplications() {
  try {
    const response = await get(`/application/user/${username}`);
    if (response.success) {
      return response.data;
    } else {
      ElMessage.success('暂无记录');
    }
  } catch (error) {
    console.error('Error fetching applications:', error);
    ElMessage.error('获取申请记录失败');
    throw error;
  }
}

export const onSubmit = async (formData) => {

  // 检查表单内容是否已填写完整
  if (!formData.dataUser) {
    ElMessage.warning('请选择数据所有方');
    return;
  }

  if (!formData.text) {
    ElMessage.warning('请输入申请说明');
    return;
  }

  if (!formData.dateTimeRange || formData.dateTimeRange.length !== 2) {
    ElMessage.warning('请选择日期时间范围');
    return;
  }

  // 准备要上传的数据
  const applicationData = {
    username: username,
    text: formData.text,
    dataUser:formData.dataUser,
    explanation:'',
    startDate: formData.dateTimeRange ? formData.dateTimeRange[0] : null,
    endDate: formData.dateTimeRange ? formData.dateTimeRange[1] : null,
    applicationType: formData.type,
    status: '等待平台审核'
  };

  try {
    // 调用接口提交数据
    const response = await post('/application/add', applicationData);
    ElMessage.success('申请提交成功');
  } catch (error) {
    console.error('Error:', error);
    ElMessage.error('申请提交失败');
  }
}

export const onSubmit1 = async (taskId) => {
  try {
    // 调用接口提交数据
    const response = await get(`/task/find_task?taskId=${taskId}`);
    if(response.success) {

      // 准备要上传的数据
      const applicationData = {
        username: username,
        text: taskId,
        dataUser:'',
        explanation:'',
        startDate: '',
        endDate: '',
        applicationType: 'confirm',
        status: '等待平台审核'
      };
      try {
        // 调用接口提交数据
        const response = await post('/application/add', applicationData);
        ElMessage.success('申请提交成功');
      } catch (error) {
        console.error('Error:', error);
        ElMessage.error('申请提交失败');
      }

    }else{
      ElMessage.error('没有找到对应的任务ID,请重新输入ID');
    }
  } catch (error) {
    ElMessage.error('申请提交失败');
  }
}