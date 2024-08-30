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
      throw new Error(response.message || 'Failed to fetch applications');
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
    startDate: formData.dateTimeRange ? formData.dateTimeRange[0] : null,
    endDate: formData.dateTimeRange ? formData.dateTimeRange[1] : null,
    applicationType: formData.type,
    status: '等待管理员审核'
  };

  try {
    // 调用接口提交数据
    const response = await post('/application/add', applicationData);
    console.log(applicationData);
    ElMessage.success('申请提交成功');
  } catch (error) {
    console.error('Error:', error);
    ElMessage.error('申请提交失败');
  }
}



/**
 * 根据状态获取步骤索引
 * @param {string} status 当前状态
 * @returns {number} 返回步骤索引
 */
export function getStepIndex(status) {
  const statusToStep = {
    '管理员审核中': 0,
    '管理员审核通过': 0,
    '管理员审核未通过': 0,
    '数据所有方审核中': 1,
    '数据所有方审核通过': 1,
    '数据所有方审核未通过': 1,
    '等待流程开启': 2,
    '等待签名': 2,
    '添加私钥签名': 2,
    '已签名等待流程结束': 2,
    '流程结束': 3
  };
  return statusToStep[status] || 0;
}

/**
 * 根据状态获取动态步骤
 * @param {string} status 当前状态
 * @returns {Array<Object>} 返回动态步骤数组
 */
export function getDynamicSteps(status) {
  const steps = [
    { title: '' }, // 管理员审核
    { title: '' }, // 数据所有方审核
    { title: '' }  // 流程
  ];

  if (status.includes("管理员")) {
    steps[0].title = status;
  } else if (status.includes("数据所有方")) {
    steps[0].title = '管理员审核通过';
    steps[1].title = status;
  } else {
    steps[0].title = '管理员审核通过';
    steps[1].title = '数据所有方审核通过';
    steps[2].title = status;
  }

  return steps; // 返回所有步骤，不再过滤掉没有标题的步骤
}

/**
 * 根据状态和步骤索引获取步骤状态
 * @param {string} status 当前状态
 * @param {number} stepIndex 当前步骤索引
 * @returns {string} 返回步骤状态（'error', 'wait', 'success', 'process'）
 */
export function getStepStatus(status, stepIndex) {
  const currentIndex = getStepIndex(status);

  if (status === '管理员审核未通过' && stepIndex === 0) {
    return 'error';
  } else if (status === '管理员审核未通过' && stepIndex > 0) {
    return 'wait';
  }

  if (status === '数据所有方审核未通过' && stepIndex === 1) {
    return 'error';
  } else if (status === '数据所有方审核未通过' && stepIndex > 1) {
    return 'wait';
  }

  if (stepIndex < currentIndex) {
    return 'success';
  } else if (stepIndex === currentIndex) {
    return 'process';
  } else {
    return 'wait';
  }
}
