import { get, post } from '@/utils/request.js'; // 导入 POST 请求函数

export const listUsers = async (tableData) => {
  try {
    const response = await get('/allUsers'); // 这里假设后端的接口路径为 /users
    if (response.success) {
      return response.data; // 返回用户信息
    } else {
      throw new Error(response.message); // 如果请求失败，抛出错误
    }
  } catch (error) {
    console.error('获取用户列表失败:', error);
    throw error; // 将错误抛出，以便在调用该方法时处理
  }
};

// 删除用户的方法
export const deleteUser = async (username) => {
  try {
    const confirmed = confirm(`确定要删除用户 ${username} 吗？`);
    if (confirmed) {
      const response = await post('/delete-user', { username });
      if (response.success) {
        alert('用户删除成功');
        return "success"; // 删除成功
      } else {
        throw new Error(response.message); // 如果请求失败，抛出错误
      }
    }
  } catch (error) {
    console.error('删除用户失败:', error);
    throw error;
  }
};

// 更新用户信息的方法
export const updateUser = async (user) => {
  try {
    const response = await post('/update-user', user); // 更新用户信息的 API
    if (response.success) {
      return response.data; // 返回更新后的数据
    } else {
      throw new Error(response.message); // 抛出错误信息
    }
  } catch (error) {
    console.error('更新用户信息失败:', error);
    throw error;
  }
};

export default {
  listUsers,
  deleteUser,
  updateUser,
};