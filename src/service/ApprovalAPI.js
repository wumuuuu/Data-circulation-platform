import { ElMessage } from 'element-plus';
import { getApplicationStatus, submitApplication } from '@/api/api.js'

const username = localStorage.getItem('username');

export const handleSubmit = async (formData, Type) => {

  // 检查表单内容是否已填写完整
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
    username: localStorage.getItem('username'), // 获取当前登录的用户名
    text: formData.text,
    startDate: formData.dateTimeRange ? formData.dateTimeRange[0] : null,
    endDate: formData.dateTimeRange ? formData.dateTimeRange[1] : null,
    applicationType: Type,
    applicationStatus: 'PENDING'
  };

  try {
    // 调用接口提交数据
    const response = await submitApplication(applicationData);
    console.log(applicationData);
    formData.text = '';
    formData.dateTimeRange = null;
    ElMessage.success('申请提交成功');
  } catch (error) {
    console.error('Error:', error);
    ElMessage.error('申请提交失败');
  }
}

export const fApplicationStatus = async (applyStep1, applyStep2,applyStep3) => {
  try {
    applyStep1.value = 0;
    applyStep2.value = 0;
    applyStep3.value = 0;

    const response = await getApplicationStatus(username);
    if (response && response.code === 200) {
      const type = response.data.applicationType;
      const status = response.data.applicationStatus;
      let step = 0;
      if (status === 'REJECT' || status === null) {
        step = 0;
      } else if (status === 'PENDING') {
        step = 1;
      } else if (status === 'APPROVED') {
        step = 2;
      }
      if (type === '签名申请') {
        applyStep1.value = step;
      } else if (type === '确权申请') {
        applyStep2.value = step;
      } else {
        applyStep3.value = step;
      }
    }
  } catch (error) {
    console.error('Error fetching application status:', error);
  }
}
