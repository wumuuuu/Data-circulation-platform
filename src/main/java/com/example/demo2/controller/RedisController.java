//package com.example.demo2.controller;
//
//import com.example.demo2.service.SignProcessService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//import java.util.List;
//
//@RestController
//@RequestMapping("/redis")
//public class RedisController {
//
//    @Autowired
//    private SignProcessService signProcessService;
//
//    /**
//     * 初始化签名任务的 API。
//     * 客户端通过此 API 初始化一个新的签名任务，传入任务 ID 和参与者列表。
//     * @param taskId 任务 ID，用于唯一标识签名任务。
//     * @param participants 参与者列表，每个参与者的用户名。
//     * @return 返回初始化任务的确认信息。
//     */
//    @PostMapping("/initializeTask")
//    public String initializeTask(@RequestParam String taskId, @RequestParam List<String> participants) {
//        // 调用服务类的方法初始化签名任务
//        signProcessService.initializeSignTask(taskId, participants);
//        return "Task initialized with ID: " + taskId;
//    }
//
//    /**
//     * 通知下一个签名者的 API。
//     * 客户端通过此 API 手动触发通知下一个签名者进行签名。
//     * @param taskId 任务 ID，用于唯一标识签名任务。
//     * @return 返回通知下一个签名者的确认信息。
//     */
//    @PostMapping("/notifyNextSigner/{taskId}")
//    public String notifyNextSigner(@PathVariable String taskId) {
//        try {
//            signProcessService.notifyNextSigner(taskId);
//            return "Notified next signer.";
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "Failed to notify signer.";
//        }
//    }
//
//    /**
//     * 存储签名结果并通知下一个签名者的 API。
//     * 客户端通过此 API 提交当前签名者的签名结果，并通知下一个签名者。
//     * @param taskId 任务 ID，用于唯一标识签名任务。
//     * @param username 当前签名者的用户名。
//     * @param signature 当前签名者的签名结果。
//     * @return 返回存储签名结果的确认信息。
//     */
//    @PostMapping("/sign/{taskId}/{username}")
//    public String storeSignatureAndNotifyNext(@PathVariable String taskId, @PathVariable String username, @RequestParam String signature) {
//        try {
//            signProcessService.storeSignatureAndNotifyNext(taskId, username, signature);
//            return "Signature stored for user: " + username;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "Failed to notify next signer.";
//        }
//    }
//}
//
