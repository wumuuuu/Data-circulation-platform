import { ref, onMounted, onUnmounted } from 'vue';
import { getTaskStatus } from '@/api/api.js'
import { SignProcess } from '@/service/SignProcessService.js'

// 用于存储签名任务状态的响应数据
export const SignTaskUpdates = ref([]);

// 用于存储当前签名任务 ID
export const signTaskId = ref(null);

// WebSocket 对象
let socket = null;

/**
 * 建立 WebSocket 连接并处理消息、错误、关闭等事件
 */
function connectWebSocket() {
  // 从 localStorage 中获取用户名，并在 WebSocket URL 中传递
  const username = encodeURIComponent(localStorage.getItem('username'));

  // 创建新的 WebSocket 连接
  socket = new WebSocket(`ws://localhost:8081/ws?username=${username}`);

  // 连接成功时的回调函数
  socket.onopen = () => {
    console.log('WebSocket connected successfully');

    // 检查本地存储中的任务ID并更新任务状态
    const storedTaskId = localStorage.getItem('signTaskId'); // 获取字符串类型的任务ID
    console.log("storedTaskId: " + storedTaskId);

    if (storedTaskId) {
      signTaskId.value = storedTaskId;
      updateTaskStatus(storedTaskId).then(() => {
        console.log('Task status updated successfully');
      }).catch((error) => {
        console.error('Error updating task status:', error);
      });
    }
  };

  // 接收到消息时的回调函数
  socket.onmessage = async (event) => {
    const messageData = JSON.parse(event.data); // 解析接收到的 JSON 数据
    console.log(messageData);
    if (messageData.taskType === '签名') {
      await SignProcess(messageData);
    }
  };

  // 连接错误时的回调函数
  socket.onerror = (error) => {
    console.error('WebSocket 连接错误:', error);
  };

  // 连接关闭时的回调函数
  socket.onclose = () => {
    console.log('WebSocket 连接关闭');
  };
}




/**
 * 查询签名任务状态并更新 SignTaskUpdates
 * @param {String} signTaskId - 签名任务 ID
 */
async function updateTaskStatus(signTaskId) {
  try {
    const response = await getTaskStatus(signTaskId);
    SignTaskUpdates.value = response; // 假设响应数据包含签名者和任务状态
    console.log('Updated task status:', response);
  } catch (error) {
    console.error('Error updating task status:', error);
  }
}

/**
 * 在组件挂载时自动连接 WebSocket，卸载时清理资源
 */
export function useWebSocket() {
  onMounted(() => {
    connectWebSocket(); // 在组件挂载时建立 WebSocket 连接
  });

  onUnmounted(() => {
    if (socket) {
      socket.close(); // 在组件卸载时关闭 WebSocket 连接
    }
  });
}
