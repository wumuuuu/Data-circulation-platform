import { ElMessage } from 'element-plus'
import { get, post } from '@/utils/request.js'
import { ref } from 'vue'

const username = localStorage.getItem('username');

// 更新申请状态的通用函数
export const update = async (Username, id, tableData, status, explanation = '') => {
  // 确保 tableData 是一个数组
  if (!tableData || !Array.isArray(tableData.value)) {
    console.error('tableData is not defined or not an array');
    return;
  }

  // 查找对应的申请记录
  const row = tableData.value.find(item => item.id === id);
  if (!row) return;

  if(row.applicationType === 'confirm') {
    status = '申请已通过';
    explanation = '等待验证';
    const applicationData = {
      signer: {
        members: []
      },
      selectFile: '',
      taskType: 'confirm',
      status:'',
      confirmId: row.text,
      username: Username,
    };

    console.log(Username);
    // 调用后端API，更新申请状态
    const response = await post('/task/create', applicationData);

    if (response.success) {
      ElMessage.success('确权任务已创建');
    } else {
      ElMessage.error('确权任务创建失败');
    }

  }

  const applicationData = {
    id: id,
    status: status, // 动态设置状态
    explanation: explanation, // 动态设置解释（可为空）
  };

  try {
    // 调用后端API，更新申请状态
    const response = await post('/application/update', applicationData);

    if (response.success) {
      // 显示成功消息，并从列表中移除该申请
      ElMessage.success(`${status === '等待数据所有方审核'|| status === '申请已通过' ? '已同意' : '已拒绝'}${row.username}的${row.applicationType}申请`);
      tableData.value = tableData.value.filter(item => item.id !== id);
    } else {
      // 如果后端返回错误，显示错误信息
      ElMessage.error(`${status === '申请已通过' ? '同意' : '拒绝'}申请失败: ${response.message}`);
    }
  } catch (error) {
    ElMessage.error(`${status === '申请已通过' ? '同意' : '拒绝'}申请时发生错误，请稍后重试。`);
  }
};

/**
 * 获取需要平台审核的申请记录
 * @returns {Promise<Array<Object>>} 返回包含该用户所有申请记录的数组
 */
export async function fetchApplications() {
  try {
    const response = await get('/application/pending2');
    if (response.success) {
      return response.data;
    } else {
      throw new Error(response.message || 'Failed to fetch pending applications');
    }
  } catch (error) {
    // 判断错误类型
    if (error.message === '未找到等待平台审核的申请记录') {
      ElMessage.success('暂无数据');
    } else {
      ElMessage.error('获取等待平台审核的申请记录失败');
    }

    throw error;
  }
}
