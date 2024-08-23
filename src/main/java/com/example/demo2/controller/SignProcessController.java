package com.example.demo2.controller;

import com.example.demo2.Mapper.DataMapper;
import com.example.demo2.Mapper.SignerMapper;
import com.example.demo2.Mapper.DataRecordMapper;
import com.example.demo2.Model.Participant;
import com.example.demo2.service.SignProcessService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/process")
public class SignProcessController {

    @Autowired
    private SignerMapper signerMapper;

    @Autowired
    private DataRecordMapper dataRecordMapper;

    @Autowired
    private SignProcessService signProcessService;

    @Autowired
    private DataMapper dataMapper;

    public SignProcessController(SignProcessService signProcessService) {
        this.signProcessService = signProcessService;
    }

    /**
     * 初始化签名任务。
     * 管理员调用此方法，提供参与者列表和任务相关参数，并生成唯一的 taskId。
     * @param requestData 包含参与者列表和任务参数的请求数据。
     * @return 返回生成的 taskId。
     */
    @PostMapping("/init")
    public Map<String, String> initializeTask(@RequestBody Map<String, Object> requestData) throws IOException {
        // 生成唯一的 taskId
        String taskId = "task-" + UUID.randomUUID();

        // 创建 ObjectMapper 实例
        ObjectMapper objectMapper = new ObjectMapper();

        // 将 "participants" 转换为 List<Participant>
        List<Participant> participants = objectMapper.convertValue(
                requestData.get("participants"),
                new TypeReference<>() {}
        );

        // 从 requestData 中提取参数 y 和 b
        String y = (String) requestData.get("y");
        String b = (String) requestData.get("b");
        String dataId = (String) requestData.get("dataId");
        String content = (String) requestData.get("content");

        // 将dataId和原数据存入data表中
        if(!dataMapper.existsDataId(dataId)){
            dataMapper.insertContent(dataId, content);
        }

        // 初始化签名任务，将任务状态和参与者列表存储到 Redis
        signProcessService.initializeSignTask(taskId, participants, y, b, dataId);

        // 向所有签名者发送 taskId
        participants.forEach(participant -> {
            try {
                signProcessService.notifySignerWithTaskId(participant.getUsername(), taskId, "签名");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 返回生成的 taskId
        return Map.of("taskId", taskId);
    }


    /**
     * 处理签名者提交的签名结果，并通知下一个签名者。
     *
     * @param formData 包含任务 ID、签名者用户名、签名结果和任务参数 y, b 的表单数据。
     * @return 返回处理结果信息。
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitSignature(@RequestBody Map<String, String> formData) {
        String taskId = formData.get("taskId");
        String y = formData.get("y");
        String b = formData.get("b");

        try {
            // 存储签名结果并通知下一个签名者
            signProcessService.storeSignatureAndNotifyNext(taskId, y, b);

            // 成功返回的响应
            return ResponseEntity.ok(Map.of("message", "签名提交成功！"));
        } catch (IOException e) {
            // 处理签名存储或通知过程中的 IO 异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "签名提交失败，服务器内部错误！"));
        } catch (Exception e) {
            // 捕获所有其他异常
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "签名提交失败，请检查提交的数据！"));
        }
    }


    /**
     * 查询特定任务的所有签名者的签名状态。
     * @param taskId 任务 ID。
     * @return 返回包含所有签名者状态的 Map。
     */
    @GetMapping("/status/{taskId}")
    public List<Map<String, String>> getTaskStatus(@PathVariable String taskId) throws JsonProcessingException {
        return signProcessService.getSignerStatus(taskId);
    }


    /**
     * 上传任务相关文件，并与任务关联。
     * @param taskId 任务 ID。
     * @param file 要上传的文件。
     * @return 返回上传结果。
     */
    @PostMapping("/upload/{taskId}")
    public String uploadFile(@PathVariable String taskId, @RequestParam("file") MultipartFile file) {
        try {
            signProcessService.storeFile(taskId, file);
            return "文件上传成功！";
        } catch (IOException e) {
            e.printStackTrace();
            return "文件上传失败：" + e.getMessage();
        }
    }
}
