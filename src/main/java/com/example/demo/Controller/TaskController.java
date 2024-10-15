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
    private TaskMapper taskMapper;  // 注入 TaskMapper，用于任务数据操作

    @Autowired
    private STUMapper stuMapper;  // 注入 STUMapper，用于签名任务用户数据操作

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);  // 日志记录器

    // 大整数 p 和 g 用于后续的加密计算
    BigInteger p = new BigInteger("132165373947571709001890899559578394061572732290158236845675979056783176833192189640519330577968623712019753279011546461561086378291703395170828826203868040544703192493236905634659492348075654172349595065574318562378095706622284475060330389667603958501055142626804746804365447731489915179943331725842802927799");
    BigInteger g = new BigInteger("436921");

    @GetMapping("/find_task")
    public APIResponse<String> findTask(@RequestParam("taskId") int taskId) {
        if(taskMapper.findTaskById(taskId) > 0)
            return APIResponse.success("找到对应流转任务");
        else
            return APIResponse.error(500, "没有找到对应流转任务");
    }

    /**
     * 处理前端的 POST 请求，创建新任务并返回操作结果
     *
     * @param createTaskRequest 包含任务信息的请求数据传输对象
     * @return 返回包含操作结果的 APIResponse 对象
     */
    @PostMapping("/create")
    public APIResponse<String> createTask(@RequestBody CreateTaskRequest createTaskRequest) {
        try {
            // 从 CreateTaskRequestDTO 中获取数据并创建 Task 对象
            Task task = new Task();
            task.setTaskType(createTaskRequest.getTaskType()); // 设置任务类型
            task.setStatus("in_progress");  // 任务初始状态
            task.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));  // 当前时间作为创建时间
            task.setFileId(createTaskRequest.getSelectFile()); // 设置文件 ID
            task.setB("0");
            task.setY("0");
            task.setX("0");

            // 将任务插入数据库，并获取返回结果
            int result = taskMapper.insert(task);

            int taskId = task.getTaskId(); // 获取新任务的 ID

            if (result > 0) {
                // 根据任务类型创建相应的用户
                if (Objects.equals(task.getTaskType(), "sign")) {
                    createSignUser(createTaskRequest, taskId); // 创建签名用户
                } else if (Objects.equals(task.getTaskType(), "determine")) {
                    // 处理确定类型任务的逻辑
                } else {
                    // 处理其他类型任务的逻辑
                }

                return APIResponse.success("任务创建成功"); // 返回成功响应
            } else {
                return APIResponse.error(500, "任务创建失败"); // 返回失败响应
            }
        } catch (Exception e) {
            return APIResponse.error(500, "发生错误: " + e.getMessage()); // 捕获异常并返回错误信息
        }
    }

    /**
     * 处理前端的 GET 请求，根据用户名查找所有进行中的任务
     *
     * @param userName 要查找的用户名
     * @return 返回包含进行中任务的 APIResponse 对象
     */
    @GetMapping("/signTask")
    public APIResponse<List<SignTaskUser>> getInProgressTasksByUserName(@RequestParam String userName) {
        try {
            List<SignTaskUser> result = stuMapper.findInProgressTasksByUserName(userName); // 查找任务
            return APIResponse.success(result); // 返回成功响应
        } catch (Exception e) {
            return APIResponse.error(500, "发生错误: " + e.getMessage()); // 捕获异常并返回错误信息
        }
    }

    /**
     * 处理前端的 POST 请求，更新任务状态并返回操作结果
     *
     * @param request 包含更新信息的请求数据传输对象
     * @return 返回包含操作结果的 APIResponse 对象
     */
    @PostMapping("/update")
    public APIResponse<String> updateTask(@RequestBody UpdateTaskRequest request) {
        try {
            String y = request.getY(); // 获取 y 值
            String b = request.getB(); // 获取 b 值

            int taskId = request.getTaskId(); // 获取任务 ID
            String userName = request.getUsername(); // 获取用户名

            // 更新当前用户的状态
            stuMapper.updateStatus(taskId, userName, "completed", "0", "0");

            // 查找下一个用户
            String nextUserName = stuMapper.findNextSigner(taskId, userName);
            if (!Objects.equals(nextUserName, "null")) {
                // 更新下一个用户的状态
                stuMapper.updateStatus(taskId, nextUserName, "in_progress", y, b);
            } else {
                // 如果没有下一个用户，标记任务为完成
                taskMapper.updateTaskFields(taskId, "completed", y, b);
            }

            return APIResponse.success("Task updated successfully"); // 返回成功响应

        } catch (NumberFormatException e) {
            // 处理数字格式转换失败
            System.err.println("Error parsing number: " + e.getMessage());
            return APIResponse.error(500, "Invalid number format for y or b");

        } catch (Exception e) {
            // 捕获其他未知错误
            System.err.println("An unexpected error occurred: " + e.getMessage());
            return APIResponse.error(500, "An error occurred while updating the task");
        }
    }

    /**
     * 创建签名用户
     *
     * @param createTaskRequest 包含用户信息的请求数据传输对象
     * @param taskId 任务 ID
     */
    public void createSignUser(CreateTaskRequest createTaskRequest, int taskId) {
        try {
            String m = createTaskRequest.getSelectFile(); // 获取文件 ID
            List<CreateTaskRequest.SignerMember> members = createTaskRequest.getSigner().getMembers(); // 提取成员列表

            // 拼接 members 的用户名和文件 ID
            StringBuilder combined = new StringBuilder(m);
            for (CreateTaskRequest.SignerMember member : members) {
                combined.append(member.getUsername()); // 拼接每个成员的用户名
            }

            // 对拼接结果进行哈希
            byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(combined.toString().getBytes());
            BigInteger hashValue = new BigInteger(1, hashBytes); // 转换为正的 BigInteger

            // 计算哈希值的平方并对 p 取模
            BigInteger x = hashValue.multiply(hashValue).mod(p);
            System.out.println("taskId: " + taskId);
            System.out.println("x: " + x);

            taskMapper.updateTaskField(taskId, String.valueOf(x));

            // 设置第一个成员的 B 和 Y 为 g 和 x，其他成员为 0
            SignTaskUser signTaskUser = new SignTaskUser();
            signTaskUser.setTaskId(taskId);
            signTaskUser.setStatus("pending"); // 初始状态为待处理
            signTaskUser.setTaskType(createTaskRequest.getTaskType()); // 设置任务类型
            signTaskUser.setFileId(createTaskRequest.getSelectFile()); // 设置文件 ID
            signTaskUser.setCompletedAt(new java.sql.Timestamp(System.currentTimeMillis())); // 当前时间作为完成时间

            for (int i = 0; i < members.size(); i++) {
                CreateTaskRequest.SignerMember member = members.get(i);

                // 对第一个成员设置 B 和 Y 为 g 和 x，其他成员设置为 0
                if (i == 0) {
                    signTaskUser.setB(String.valueOf(g)); // 设置 B
                    signTaskUser.setY(String.valueOf(x)); // 设置 Y
                    signTaskUser.setStatus("in_progress"); // 设置状态为进行中
                } else {
                    signTaskUser.setB("0"); // 其他成员 B 设置为 0
                    signTaskUser.setY("0"); // 其他成员 Y 设置为 0
                    signTaskUser.setStatus("pending"); // 状态为待处理
                }

                signTaskUser.setUserName(member.getUsername()); // 设置用户名

                // 设置下一个签名者
                if (i < members.size() - 1) {
                    CreateTaskRequest.SignerMember nextMember = members.get(i + 1);
                    signTaskUser.setNextSigner(nextMember.getUsername());
                } else {
                    signTaskUser.setNextSigner("null"); // 最后一个成员没有下一个签名者
                }
                stuMapper.insertTaskUser(signTaskUser); // 插入签名用户信息
            }

        } catch (NoSuchAlgorithmException e) {
            logger.error("哈希算法异常", e); // 记录哈希算法异常
        } catch (Exception e) {
            logger.error("任务处理发生异常", e); // 记录其他异常
        }
    }
}
