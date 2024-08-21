package com.example.demo2.service;

import com.example.demo2.Model.Participant;
import com.example.demo2.config.WSConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class SignProcessService {

    private final Path fileStorageLocation;
    private final WSConfig wsConfig;  // 使用 final 修饰，确保在构造函数中注入

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public SignProcessService(WSConfig wsConfig) {  // 使用构造函数注入 wsConfig
        this.wsConfig = wsConfig;
        this.fileStorageLocation = Paths.get("uploaded_files")
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * 初始化签名任务。
     * 该方法在 Redis 中存储任务的状态和参与者列表。
     * @param taskId 任务 ID，用于唯一标识签名任务。
     * @param participants 参与者列表，每个参与者的用户名。
     */
    public void initializeSignTask(String taskId, List<Participant> participants, String y, String b) {
        try {
            // 将参数 y 和 b 存储到 Redis
            redisTemplate.opsForHash().put("Task:" + taskId, "y", y);
            redisTemplate.opsForHash().put("Task:" + taskId, "b", b);

            // 提取参与者列表中的用户名和角色，并存储在 Redis 中
            for (Participant participant : participants) {
                String username = participant.getUsername();
                String role = participant.getRole();

                // 将用户名和角色存储为一个 JSON 字符串
                Map<String, String> userRoleMap = new HashMap<>();
                userRoleMap.put("username", username);
                userRoleMap.put("role", role);
                String userRoleJson = new ObjectMapper().writeValueAsString(userRoleMap);

                // 将 JSON 字符串存储在 Redis 中的列表中
                redisTemplate.opsForList().rightPush("Task:" + taskId + ":participants", userRoleJson);
            }

            // 初始化签名人索引为0，并存储在 Redis 中
            redisTemplate.opsForHash().put("Task:" + taskId, "currentIndex", "0");
            redisTemplate.opsForHash().put("Task:" + taskId, "status", "start");

            // 通知第一个签名者并将其状态更新为“正在签名”
            if (!participants.isEmpty()) {
                Participant firstParticipant = participants.get(0);  // 获取第一个签名者
                String firstUsername = firstParticipant.getUsername();

                // 将第一个签名者的状态设置为“正在签名”
                redisTemplate.opsForHash().put("Task:" + taskId + ":status", firstUsername, "正在签名");

                // 通知第一个签名者开始签名
                wsConfig.getWebSocketHandler().sendMessageToUser(firstUsername, taskId, "签名", "start" , y, b);
            }

        } catch (JsonProcessingException e) {
            // 处理 JSON 处理异常
            throw new RuntimeException("Error processing JSON", e);
        } catch (Exception e) {
            // 捕获所有其他异常
            throw new RuntimeException("Unexpected error during sign task initialization", e);
        }
    }


    /**
     * 存储签名结果并通知下一个签名者。
     * 该方法在 Redis 中存储签名结果，并通过 WebSocket 通知下一个签名者。
     * @param taskId 任务 ID，用于唯一标识签名任务。
     * @param y 参数 y。
     * @param b 参数 b。
     * @throws IOException 如果 WebSocket 消息发送失败。
     */
    public void storeSignatureAndNotifyNext(String taskId, String y, String b) throws IOException {
        Logger logger = LoggerFactory.getLogger(SignProcessService.class);

        // 将 y 和 b 参数存储到 Redis 中
        redisTemplate.opsForHash().put("Task:" + taskId, "y", y);
        redisTemplate.opsForHash().put("Task:" + taskId, "b", b);

        // 获取当前签名人索引
        Object indexObj = redisTemplate.opsForHash().get("Task:" + taskId, "currentIndex");

        if (indexObj == null) {
            throw new IllegalStateException("currentIndex not found in Redis for task: " + taskId);
        }

        // 解析 currentIndex
        int currentIndex;
        try {
            currentIndex = Integer.parseInt((String) indexObj);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Unexpected String format for currentIndex in Redis: " + indexObj, e);
        }

        // 获取参与者列表
        List<Object> participants = redisTemplate.opsForList().range("Task:" + taskId + ":participants", 0, -1);

        if (currentIndex >= participants.size()) {
            throw new IndexOutOfBoundsException("currentIndex exceeds participant list size");
        }

        // 获取当前签名者的 JSON 字符串
        String currentSignerJson = (String) participants.get(currentIndex);
        logger.info("Current signer JSON: {}", currentSignerJson);

        // 解析 JSON 字符串以获取用户名
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> currentSignerMap = objectMapper.readValue(currentSignerJson, new TypeReference<Map<String, String>>() {});
        String currentSigner = currentSignerMap.get("username");

        // 更新当前签名者的状态为“已签名”
        redisTemplate.opsForHash().put("Task:" + taskId + ":status", currentSigner, "已签名");

        // 检查是否还有下一个签名者
        if (currentIndex < participants.size() - 1) {
            // 更新当前签名人索引
            currentIndex++;
            redisTemplate.opsForHash().put("Task:" + taskId, "currentIndex", String.valueOf(currentIndex));

            // 获取下一个签名者的 JSON 字符串
            String nextSignerJson = (String) participants.get(currentIndex);
            logger.info("Next signer JSON: {}", nextSignerJson);

            // 解析 JSON 字符串以获取用户名
            Map<String, String> nextSignerMap = objectMapper.readValue(nextSignerJson, new TypeReference<Map<String, String>>() {});
            String nextSigner = nextSignerMap.get("username");
            logger.info("Next signer: {}", nextSigner);

            // 更新下一个签名者的状态为“正在签名”
            redisTemplate.opsForHash().put("Task:" + taskId + ":status", nextSigner, "正在签名");

            // 通过 WebSocket 通知下一个签名者
            wsConfig.getWebSocketHandler().sendMessageToUser(nextSigner, taskId, "签名","start", y, b);
        } else {
            // 如果没有下一个签名者，更新任务状态为 "complete"
            redisTemplate.opsForHash().put("Task:" + taskId, "status", "complete");
            // 此处可以根据需要广播任务完成的消息

            // 广播任务完成的消息给所有参与者
            for (Object participant : participants) {
                String participantJson = (String) participant;
                Map<String, String> participantMap = objectMapper.readValue(participantJson, new TypeReference<Map<String, String>>() {});
                String username = (participantMap.get("username"));
                wsConfig.getWebSocketHandler().sendMessageToUser(username, taskId, "签名", "complete",null, null);
            }
        }
    }




    /**
     * 通过 WebSocket 向签名者发送 taskId。
     * @param username 签名者的用户名。
     * @param taskId 要发送的任务 ID。
     * @param taskStatus 要发送的任务类型。
     * @throws IOException 如果 WebSocket 消息发送失败。
     */
    public void notifySignerWithTaskId(String username, String taskId, String taskStatus) throws IOException {
        // 通过 WebSocket 向签名者发送消息
        wsConfig.getWebSocketHandler().sendMessageToUser(username, taskId, taskStatus, "start",null, null);
    }


    /**
     * 获取任务状态及进展情况。
     * @param taskId 任务 ID，用于唯一标识签名任务。
     * @return 返回任务的状态及签名进展情况。
     */
    public List<Map<String, String>> getSignerStatus(String taskId) throws JsonProcessingException {
        // 获取参与者列表
        List<Object> participants = redisTemplate.opsForList().range("Task:" + taskId + ":participants", 0, -1);

        // 创建一个 List 来存储每个参与者的状态信息
        List<Map<String, String>> signerStatusList = new ArrayList<>();

        // 遍历参与者列表并获取他们的状态
        for (Object participant : participants) {
            String participantJson = convertObjectToString(participant);

            // 将 JSON 字符串转换为 Map
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> participantMap = objectMapper.readValue(participantJson, new TypeReference<Map<String, String>>() {});

            String username = participantMap.get("username");
            String status = (String) redisTemplate.opsForHash().get("Task:" + taskId + ":status", username);

            // 更新状态信息
            participantMap.put("status", status != null ? status : "未签名");

            // 将参与者的状态信息添加到列表中
            signerStatusList.add(participantMap);
        }

        return signerStatusList;
    }


    /**
     * 将文件存储到服务器，并将文件路径与任务 ID 关联存储到 Redis 中。
     * @param taskId 任务 ID。
     * @param file 要存储的文件。
     * @throws IOException 如果文件存储失败。
     */
    public void storeFile(String taskId, MultipartFile file) throws IOException {
        String fileName = taskId + "_" + file.getOriginalFilename();

        // 将文件保存到指定目录
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation);

        // 将文件路径存储到 Redis 中
        redisTemplate.opsForHash().put("Task:" + taskId, "filePath", targetLocation.toString());
    }

    /**
     * 根据任务 ID 从文件系统加载文件，供签名者下载。
     * @param taskId 任务 ID。
     * @return 返回文件资源。
     */
    public FileSystemResource loadFileAsResource(String taskId) {
        try {
            // 从 Redis 中获取文件路径
            String filePath = (String) redisTemplate.opsForHash().get("Task:" + taskId, "filePath");

            Path file = Paths.get(filePath);
            FileSystemResource resource = new FileSystemResource(file);

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + filePath);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File not found", ex);
        }
    }

    private String convertObjectToString(Object obj) throws JsonProcessingException {
        if (obj instanceof LinkedHashMap) {
            return new ObjectMapper().writeValueAsString(obj);
        }
        return obj.toString();
    }
}
