import { ElMessage } from 'element-plus'
import { Application, checkUserExists, getApplications, getHistoricalSigners } from '@/api/api.js'
import CryptoJS from 'crypto-js' // 根据你的项目实际路径调整

// 同意申请
export const handleAgree = async (id, tableData) => {
  // 查找对应的申请记录
  const row = tableData.value.find(item => item.id === id);
  if (!row) return;

  const applicationData = {
    id: id,
    applicationStatus: 'APPROVED', // 确保与后端的字段名一致
    username: row.username, // 添加必要的字段
    applicationType: row.applicationType,
  };

  console.log(applicationData);

  try {
    // 调用后端API，同意申请
    await Application(applicationData);

    // 显示成功消息，并从列表中移除该申请
    ElMessage.success(`已通过${row.username}的${row.applicationType}`);
    tableData.value = tableData.value.filter(item => item.id !== id);
  } catch (error) {
    console.error('Error approving application:', error);
  }
};

//拒绝申请
export const handleReject = async (id, tableData) => {
  const row = tableData.value.find(item => item.id === id);
  if (!row) return;
  const applicationData = {
    id: id,
    applicationStatus: 'REJECT', // 确保与后端的字段名一致
    username: row.username, // 添加必要的字段
    applicationType: row.applicationType,
  };

  try {
    // 调用后端API，同意申请
    await Application(applicationData);    // 显示成功消息，并从列表中移除该申请
    ElMessage.success(`已拒绝${row.username}的${row.applicationType}`);
    tableData.value = tableData.value.filter(item => item.id !== id);
  } catch (error) {
    console.error('Error approving application:', error);
  }
}

// 向后端发送请求，查找是否存在此用户
export const checkUser = async (form, memberSearch) =>{
  const userAlreadyExists = form.value.members.some(member => member.username === memberSearch);
  if (userAlreadyExists) {
    ElMessage.error('该用户已在成员列表中，不能重复添加。');
    memberSearch = ''; // 清空输入框
    return; // 终止添加流程
  }
  try {
    // 向后端发送请求，查找是否存在此用户
    const response = await checkUserExists(memberSearch);
    if (response.code === 2) {
      // 如果用户存在，将其添加到成员列表中
      form.value.members.push({ username: memberSearch});
      memberSearch = ''; // 清空输入框
      ElMessage.success('用户添加成功');
    } else if (response.code === 0) {
      ElMessage.error('用户名不能为空');
    }else{
      // 如果用户不存在，提示用户
      ElMessage.error('用户名不存在');
    }
  } catch (error) {
    // 如果请求出错，提示错误信息
    alert('无法检查用户，请稍后重试。');
  }
}

// 获取申请列表
export const ListApplications = async (tableData) =>{
  try {
    const response = await getApplications();
    if (response && response.code === 200) {
      tableData.value = response.data;
    } else {
      console.error('Error fetching applications:', response);
    }
  } catch (error) {
    console.error('Error fetching applications:', error);
  }
}

// 处理文件的哈希值计算、查询历史签名者并更新签名者列表
export const addUser = async (file, form) => {
  try {
    const dataId = await calculateDataId(file); // 计算数据ID
    await fetchSigners(dataId, form);  // 查询历史签名者并更新列表
    ElMessage.success("历史签名者已成功添加到列表");
  } catch (error) {
    ElMessage.error("处理文件时出错，请重试");
    console.error(error);
  }
};

// 计算文件的哈希值（数据ID）
const calculateDataId = async (file) => {
  const reader = new FileReader();

  return new Promise((resolve, reject) => {
    reader.onload = (event) => {
      const fileContent = event.target.result;
      const hash = CryptoJS.SHA256(CryptoJS.lib.WordArray.create(fileContent));
      resolve(hash.toString(CryptoJS.enc.Hex));
    };

    reader.onerror = (error) => {
      reject(error);
    };

    reader.readAsArrayBuffer(file);
  });
};

// 根据数据ID从后端获取历史签名者并添加到签名者列表中
const fetchSigners = async (dataId, form) => {
  try {
    const signers = await getHistoricalSigners(dataId);
    form.value.members.push(...signers);  // 更新签名者列表
  } catch (error) {
    console.error("Error fetching signers:", error);
    throw error;
  }
};