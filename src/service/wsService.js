import { ref, onMounted } from 'vue';
import Stomp from 'stompjs';

// 当前用户ID，实际应用中应从登录信息中获取，这里从 localStorage 中获取已登录的用户名
export const currentUserId = localStorage.getItem('username');
export const currentTaskId = ref(null);// 当前任务ID
export const taskUpdates = ref([]);// 存储接收到的任务更新

// 声明 stompClient 变量，用于存储 STOMP 客户端实例
let stompClient = null;

/**
 * 建立 WebSocket 连接并配置 STOMP 客户端
 */
export function connectWebSocket() {
  // 创建一个新的 WebSocket 连接，连接到指定的 WebSocket 服务器端点
  const socket = new WebSocket('ws://localhost:8081/ws');

  // 使用 STOMP 客户端包装 WebSocket 连接
  stompClient = Stomp.over(socket);

  // 连接 STOMP 服务器，成功时调用 onConnected，出错时调用 onError
  stompClient.connect({}, onConnected, onError);
}

/**
 * STOMP 客户端连接成功后的回调函数
 */
function onConnected() {
  // 订阅一个 STOMP 主题，当服务器在该主题上发送消息时调用 onMessageReceived
  stompClient.subscribe('/topic/tasks', onMessageReceived);
}

/**
 * 当收到 STOMP 消息时处理的回调函数
 * @param {Object} payload - STOMP 消息负载，包含消息的主体内容
 */
function onMessageReceived(payload) {
  // 将消息内容从 JSON 字符串解析为 JavaScript 对象
  const task = JSON.parse(payload.body);

  // 如果任务的 taskId 与当前任务ID匹配，并且当前用户是任务的参与者之一，则处理该任务
  if (task.taskId === currentTaskId.value && task.participants.includes(currentUserId)) {
    // 将任务添加到 taskUpdates 数组中
    taskUpdates.value.push(task);
    console.log('Received task update:', task);
    // 在这里可以进一步处理任务，如更新页面状态等
  }
}

/**
 * 当 WebSocket 连接发生错误时调用的回调函数
 * @param {Object} error - 错误信息
 */
function onError(error) {
  // 打印错误信息到控制台
  console.error('WebSocket connection error:', error);
}

/**
 * 自动连接 WebSocket，当组件挂载时调用
 * 使用 onMounted 生命周期钩子确保在组件挂载时调用 connectWebSocket
 */
export function useWebSocket() {
  onMounted(() => {
    connectWebSocket();
  });
}
