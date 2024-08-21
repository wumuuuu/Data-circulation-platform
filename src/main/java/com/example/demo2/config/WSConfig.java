package com.example.demo2.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSocket
public class WSConfig implements WebSocketConfigurer {

    @Autowired
    private StringRedisTemplate redisTemplate; // 注入 Redis 模板，用于存储和检索未发送的消息

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册 WebSocket 处理器，路径为 "/ws"，允许跨域访问
        registry.addHandler(new MyWebSocketHandler(redisTemplate), "/ws").setAllowedOrigins("*");
    }

    @Bean
    public MyWebSocketHandler getWebSocketHandler() {
        // 通过此方法返回 MyWebSocketHandler 的实例
        return new MyWebSocketHandler(redisTemplate);
    }

    public static class MyWebSocketHandler extends TextWebSocketHandler {

        // 使用线程安全的 Map 来存储 WebSocket 会话和用户名的关联
        private static final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

        private final StringRedisTemplate redisTemplate;

        // 使用构造函数注入 RedisTemplate
        public MyWebSocketHandler(StringRedisTemplate redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            // 假设用户名通过 WebSocket URL 参数传递，例如 /ws?username=zzzzz
            String username = session.getUri().getQuery().split("=")[1];
            // 将会话和用户名关联起来
            userSessions.put(username, session);

            // 检查 Redis 中是否有未发送的消息，如果有，发送给用户
            List<String> pendingMessages = redisTemplate.opsForList().range("messages:" + username, 0, -1);
            if (pendingMessages != null && !pendingMessages.isEmpty()) {
                for (String msg : pendingMessages) {
                    session.sendMessage(new TextMessage(msg)); // 发送未发送的消息
                }
                // 发送完毕后清空 Redis 中的未发送消息列表
                redisTemplate.delete("messages:" + username);
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            // 从 userSessions 中移除已经关闭的会话
            userSessions.values().remove(session);
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            // 处理从客户端接收到的消息（此处为示例）
            System.out.println("Received message: " + message.getPayload());
        }

        /**
         * 向特定用户名的客户端发送消息
         * 如果用户不在线，消息将会存储在 Redis 中，待用户上线时发送
         * @param username 用户名
         * @param taskId  任务ID
         * @param taskType  任务类型
         * @param taskStatus  任务类型
         * @param y  参数
         * @param b  参数
         * @throws IOException 如果消息发送失败
         */
        public void sendMessageToUser(String username, String taskId, String taskType, String taskStatus, String y, String b) throws IOException {
            WebSocketSession session = userSessions.get(username);
            ObjectMapper objectMapper = new ObjectMapper();
            // 构建消息内容
            Map<String, String> message = new HashMap<>();
            if (taskId != null) {
                message.put("taskId", taskId);
            }
            if (y != null && b != null) {
                message.put("y", y);
                message.put("b", b);
            }
            message.put("taskType", taskType);
            message.put("taskStatus", taskStatus);
            String jsonMessage = objectMapper.writeValueAsString(message);

            if (session != null && session.isOpen()) {
                // 如果用户在线，直接发送消息
                session.sendMessage(new TextMessage(jsonMessage));
            } else {
                // 如果用户未连接，将消息存储在 Redis 中，等待用户重新连接时发送
                redisTemplate.opsForList().rightPush("messages:" + username, jsonMessage);
                System.out.println("User " + username + " is not connected. Message saved to Redis.");
            }
        }
    }
}
