package com.example.demo.Controller;

import com.example.demo.Mapper.STUMapper;
import com.example.demo.Mapper.TaskMapper;
import com.example.demo.Model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;



@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private STUMapper stuMapper;

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);


    BigInteger p = new BigInteger("132165373947571709001890899559578394061572732290158236845675979056783176833192189640519330577968623712019753279011546461561086378291703395170828826203868040544703192493236905634659492348075654172349595065574318562378095706622284475060330389667603958501055142626804746804365447731489915179943331725842802927799");
    BigInteger g = new BigInteger("436921");

    @PostMapping("/create")
    public APIResponse<String> createTask(@RequestBody CreateTaskRequestDTO createTaskRequest) {
        try {
            // 从 CreateTaskRequestDTO 中获取数据
            Task task = new Task();

            // 设置任务类型、状态等字段
            task.setTaskType(createTaskRequest.getTaskType()); // 从 DTO 中获取任务类型
            task.setStatus("进行中");  // 任务初始状态
            task.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));  // 当前时间作为创建时间
            task.setFileId(createTaskRequest.getSelectFile());

            int result = taskMapper.insert(task);

            System.out.println(task);

            int taskId = task.getTaskId();

            if (result > 0) {

                if(Objects.equals(task.getTaskType(), "sign")){
                    createSignUser(createTaskRequest, taskId);
                } else if(Objects.equals(task.getTaskType(), "determine")){

                }else {

                }

                return APIResponse.success("任务创建成功");
            } else {
                return APIResponse.error(500, "任务创建失败");
            }
        } catch (Exception e) {
            return APIResponse.error(500, "发生错误: " + e.getMessage());
        }
    }

    // 根据用户名查找所有任务
    @GetMapping("/signTask")
    public APIResponse<List<SignTaskUser>> getInProgressTasksByUserName(@RequestParam String userName) {
        try {
            List<SignTaskUser> result = stuMapper.findInProgressTasksByUserName(userName);
            return APIResponse.success(result);
        } catch (Exception e) {
            return APIResponse.error(500, "发生错误: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public APIResponse<String> updateTask(@RequestBody UpdateTaskRequest request) {
        try {
            BigInteger y = new BigInteger(request.getY());
            BigInteger b = new BigInteger(request.getB());

            System.out.println("Task ID: " + request.getTaskId());
            System.out.println("Username: " + request.getUsername());
            System.out.println("Y: " + y);
            System.out.println("B: " + b);

            return APIResponse.success("Task updated successfully");

        } catch (NumberFormatException e) {
            // 处理 BigInteger 转换失败的情况
            System.err.println("Error parsing BigInt: " + e.getMessage());
            return APIResponse.error(500,"Invalid number format for y or b");

        } catch (Exception e) {
            // 捕获其他未知错误
            System.err.println("An unexpected error occurred: " + e.getMessage());
            return APIResponse.error(500,"An error occurred while updating the task");
        }
    }


    public void createSignUser(CreateTaskRequestDTO createTaskRequest, int taskId) {
        try {
            String m = createTaskRequest.getSelectFile();
            List<CreateTaskRequestDTO.SignerMember> members = createTaskRequest.getSigner().getMembers(); // 提取 members

            // 拼接 members 的 username 和 m
            StringBuilder combined = new StringBuilder(m);
            for (CreateTaskRequestDTO.SignerMember member : members) {
                combined.append(member.getUsername()); // 拼接每个成员的 username
            }

            // 对拼接结果进行哈希
            byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(combined.toString().getBytes());
            BigInteger hashValue = new BigInteger(1, hashBytes); // 转换为正的 BigInteger

            // 计算哈希值的平方并对 p 取模
            BigInteger x = hashValue.multiply(hashValue).mod(p);

            // 设置第一个成员的 B 和 Y 为 g 和 x，其他成员为 0
            SignTaskUser signTaskUser = new SignTaskUser();
            signTaskUser.setTaskId(taskId);
            signTaskUser.setStatus("pending");
            signTaskUser.setTaskType(createTaskRequest.getTaskType());
            signTaskUser.setFileId(createTaskRequest.getSelectFile());
            signTaskUser.setCompletedAt(new java.sql.Timestamp(System.currentTimeMillis()));

            for (int i = 0; i < members.size(); i++) {
                CreateTaskRequestDTO.SignerMember member = members.get(i);

                // 对第一个成员设置 B 和 Y 为 g 和 x，其他成员设置为 0
                if (i == 0) {
                    signTaskUser.setB(String.valueOf(g));
                    signTaskUser.setY(String.valueOf(x));
                    signTaskUser.setStatus("in_progress");
                } else {
                    signTaskUser.setB("0");
                    signTaskUser.setY("0");
                    signTaskUser.setStatus("pending");
                }

                signTaskUser.setUserName(member.getUsername());

                if (i < members.size() - 1) {
                    CreateTaskRequestDTO.SignerMember nextMember = members.get(i + 1);
                    signTaskUser.setNextSigner(nextMember.getUsername());
                } else {
                    signTaskUser.setNextSigner(null);
                }
                stuMapper.insertTaskUser(signTaskUser);
            }

        } catch (NoSuchAlgorithmException e) {
            logger.error("哈希算法异常", e);
        } catch (Exception e) {
            logger.error("任务处理发生异常", e);
        }
    }



}
