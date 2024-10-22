package com.example.demo.Controller;

import com.example.demo.Mapper.ApplicationMapper;
import com.example.demo.Mapper.CTUMapper;
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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskMapper taskMapper;  // 注入 TaskMapper，用于任务数据操作

    @Autowired
    private STUMapper stuMapper;  // 注入 STUMapper，用于签名任务用户数据操作

    @Autowired
    private CTUMapper ctuMapper;  // 注入 STUMapper，用于确权任务用户数据操作

    @Autowired
    private ApplicationMapper applicationMapper;

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);  // 日志记录器

    // 大整数 p 和 g 用于后续的加密计算
    BigInteger p = new BigInteger("132165373947571709001890899559578394061572732290158236845675979056783176833192189640519330577968623712019753279011546461561086378291703395170828826203868040544703192493236905634659492348075654172349595065574318562378095706622284475060330389667603958501055142626804746804365447731489915179943331725842802927799");
    BigInteger g = new BigInteger("436921");

    @GetMapping("/find_task")
    public APIResponse<String> findTask(@RequestParam("taskId") int taskId) {
        if(taskMapper.findTaskById(taskId) != null)
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
            task.setUsername(createTaskRequest.getUsername());

            // 创建 SecureRandom 实例
            SecureRandom random = new SecureRandom();
            // 生成 1024 位随机数
            BigInteger e1 = new BigInteger(1024, random);
            BigInteger e2 = new BigInteger(1024, random);
            if (Objects.equals(task.getTaskType(), "sign")) {
                task.setFileId(createTaskRequest.getSelectFile()); // 设置文件 ID
                task.setConfirmId("");
                task.setB("");
                task.setY("");
                task.setX("");
                task.setE1("");
                task.setE2("");
            } else if (Objects.equals(task.getTaskType(), "confirm")) {
                task.setConfirmId(createTaskRequest.getConfirmId());
                Task task1 = taskMapper.findTaskById(Integer.parseInt(task.getConfirmId()));
                task.setFileId(task1.getFileId());
                task.setB(task1.getB());
                task.setY(task1.getY());
                task.setX(task1.getX());
                task.setE1(String.valueOf(e1));
                task.setE2(String.valueOf(e2));

            } else {
                // 处理其他类型任务的逻辑
            }

            System.out.println(task);

            // 将任务插入数据库，并获取返回结果
            int result = taskMapper.insert(task);

            int taskId = task.getTaskId(); // 获取新任务的 ID

            if (result > 0) {
                // 根据任务类型创建相应的用户
                if (Objects.equals(task.getTaskType(), "sign")) {
                    createSignUser(createTaskRequest, taskId); // 创建签名用户
                } else if (Objects.equals(task.getTaskType(), "confirm")) {
                    createConfirmUser(Integer.parseInt(createTaskRequest.getConfirmId()), taskId, e1, e2);
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
    @GetMapping("/getTask")
    public APIResponse<List<Handle>> getInProgressTasksByUserName(@RequestParam String userName) {
        try {
            List<SignTaskUser> signTaskUsers = stuMapper.findInProgressTasksByUserName(userName); // 查找任务
            List<ConfirmTaskUser> confirmTaskUsers = ctuMapper.findInProgressTasksByUserName(userName);

            List<Handle> handles = new ArrayList<>(); // 创建一个空的 Handle 列表

            // 将 signTaskUsers 转换为 Handle
            for (SignTaskUser signTaskUser : signTaskUsers) {
                Handle handle = new Handle();
                handle.setTaskId(signTaskUser.getTaskId());
                handle.setFileId(signTaskUser.getFileId());
                handle.setTaskType(signTaskUser.getTaskType());
                handle.setStatus(signTaskUser.getStatus());
                handle.setCompletedAt(signTaskUser.getCompletedAt());
                handle.setB(signTaskUser.getB());
                handle.setY(signTaskUser.getY());
                handles.add(handle);
            }

            // 将 confirmTaskUsers 转换为 Handle
            for (ConfirmTaskUser confirmTaskUser : confirmTaskUsers) {
                Handle handle = new Handle();
                handle.setTaskId(confirmTaskUser.getTaskId());
                handle.setTaskType(confirmTaskUser.getTaskType());
                handle.setStatus(confirmTaskUser.getStatus());
                handle.setCompletedAt(confirmTaskUser.getCompletedAt());
                handle.setD(confirmTaskUser.getD());
                handles.add(handle);
            }

            return APIResponse.success(handles); // 返回成功响应
        } catch (Exception e) {
            return APIResponse.error(500, "发生错误: " + e.getMessage()); // 捕获异常并返回错误信息
        }
    }

    /**
     * 处理前端的 POST 请求，更新签名任务状态并返回操作结果
     *
     * @param request 包含更新信息的请求数据传输对象
     * @return 返回包含操作结果的 APIResponse 对象
     */
    @PostMapping("/signUpdate")
    public APIResponse<String> signUpdateTask(@RequestBody TaskRequest request) {
        try {
            String y = request.getY(); // 获取 y 值
            String b = request.getB(); // 获取 b 值
            String userName = request.getUsername(); // 获取用户名
            int taskId = request.getTaskId(); // 获取任务 ID


            // 更新当前用户的状态
            stuMapper.updateStatus(taskId, userName, "completed", "0", "0");

            // 查找当前用户的 signerNumber
            int currentSignerNumber = stuMapper.findSignerNumber(taskId, userName);

            SignTaskUser nextUser = stuMapper.findNextSigner(taskId, currentSignerNumber + 1);

            if (nextUser != null) {
                stuMapper.updateStatus(taskId, nextUser.getUserName(), "in_progress", y, b);
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
     * 处理前端的 POST 请求，更新确权任务状态并返回操作结果
     *
     * @param request 包含更新信息的请求数据传输对象
     * @return 返回包含操作结果的 APIResponse 对象
     */
    @PostMapping("/confirmUpdate")
    public APIResponse<String> confirmUpdateTask(@RequestBody TaskRequest request) {
        try{
            String d = request.getD(); // 获取 d 值

            int taskId = request.getTaskId(); // 获取任务 ID
            String userName = request.getUsername(); // 获取用户名

            // 更新当前用户的状态
            String t = ctuMapper.findTask(taskId, userName);
            ctuMapper.updateStatus(taskId, userName, "completed", t);

            // 查找当前用户的 ConfirmNumber
            int currentConfirmNumber = ctuMapper.findConfirmNumber(taskId, userName);

            ConfirmTaskUser nextUser = ctuMapper.findNextConfirm(taskId, currentConfirmNumber + 1);

            if (nextUser != null) {
                ctuMapper.updateStatus(taskId, nextUser.getUserName(), "in_progress", d);
                return APIResponse.success("Task updated successfully"); // 返回成功响应
            } else {
                // 如果没有下一个用户，标记任务为完成
                Task task = taskMapper.findTaskById(taskId);
                BigInteger x = new BigInteger(task.getX());
                BigInteger e1 = new BigInteger(task.getE1());
                BigInteger e2 = new BigInteger(task.getE2());
                BigInteger D = new BigInteger(d);
                BigInteger k = x.modPow(e1, p).multiply(g.modPow(e2, p)).mod(p);
                String text = task.getConfirmId();
                String Username = task.getUsername();

                if(k.equals(D)){
                    applicationMapper.updateApplicationStatus(Username, text, "确权验证成功" );
                    taskMapper.updateTaskFields(taskId, "completed", task.getY(), task.getB());
                    return APIResponse.success("Task updated successfully"); // 返回成功响应
                }

                applicationMapper.updateApplicationStatus(Username, text, "确权验证失败" );
                taskMapper.updateTaskFields(taskId, "completed", task.getY(), task.getB());
                return APIResponse.error(500, "确权验证失败");

            }
        } catch (NumberFormatException e){
            System.err.println("Error parsing number: " + e.getMessage());
            return APIResponse.error(500, "Invalid number format for y or b");
        } catch (Exception e){
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

            taskMapper.updateTaskField(taskId, String.valueOf(x));

            // 设置第一个成员的 B 和 Y 为 g 和 x，其他成员为 0
            for (int i = 0; i < members.size(); i++) {
                CreateTaskRequest.SignerMember member = members.get(i);
                SignTaskUser signTaskUser = new SignTaskUser();

                signTaskUser.setTaskId(taskId);
                signTaskUser.setStatus(i == 0 ? "in_progress" : "pending"); // 第一个成员进行中，其他为待处理
                signTaskUser.setTaskType(createTaskRequest.getTaskType()); // 设置任务类型
                signTaskUser.setFileId(createTaskRequest.getSelectFile()); // 设置文件 ID
                signTaskUser.setCompletedAt(new java.sql.Timestamp(System.currentTimeMillis())); // 当前时间作为完成时间

                // 对第一个成员设置 B 和 Y 为 g 和 x，其他成员设置为 0
                signTaskUser.setB(i == 0 ? String.valueOf(g) : "0"); // 设置 B
                signTaskUser.setY(i == 0 ? String.valueOf(x) : "0"); // 设置 Y

                // 设置编号
                signTaskUser.setSignerNumber(i + 1); // 设置编号，从 1 开始

                signTaskUser.setUserName(member.getUsername()); // 设置用户名

                System.out.println(signTaskUser);

                stuMapper.insertTaskUser(signTaskUser); // 插入签名用户信息
            }

        } catch (NoSuchAlgorithmException e) {
            logger.error("哈希算法异常", e); // 记录哈希算法异常
        } catch (Exception e) {
            logger.error("任务处理发生异常", e); // 记录其他异常
        }
    }


    /**
     * 创建确权用户
     *
     * @param confirmId 确权的任务ID
     * @param taskId 任务 ID
     */
    public void createConfirmUser(int confirmId, int taskId, BigInteger e1, BigInteger e2) {
        try {
            List<SignTaskUser> confirmUser = stuMapper.findUserNamesAndSignerNumbersByTaskId(confirmId);
            Task task = taskMapper.findTaskById(taskId);
            BigInteger y = new BigInteger(task.getY());
            BigInteger b = new BigInteger(task.getB());
            int i = -1;

            // 模幂运算
            BigInteger Y = y.modPow(e1, p);
            BigInteger B = b.modPow(e2, p);
            BigInteger c = Y.multiply(B).mod(p);

            if (confirmUser != null) {
                for (SignTaskUser user : confirmUser) {
                    i++;
                    ConfirmTaskUser confirm = new ConfirmTaskUser();
                    confirm.setTaskId(taskId);
                    confirm.setUserName(user.getUserName());
                    confirm.setConfirmNumber(user.getSignerNumber());
                    confirm.setCompletedAt(new java.sql.Timestamp(System.currentTimeMillis()));

                    // 第一个用户设置 c，其他用户设置 0
                    confirm.setD(i == 0 ? String.valueOf(c) : "0");
                    confirm.setStatus(i == 0 ? "in_progress" : "pending");
                    confirm.setTaskType("confirm");

                    // 插入记录到数据库
                    System.out.println(confirm);
                    ctuMapper.insertTaskUser(confirm);
                }
            }
        } catch (Exception e) {
            // 捕获所有异常并输出错误信息
            System.err.println("An error occurred while creating confirm users: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
