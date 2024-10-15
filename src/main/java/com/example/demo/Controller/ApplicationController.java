package com.example.demo.Controller;

import com.example.demo.Mapper.ApplicationMapper;
import com.example.demo.Model.Application;
import com.example.demo.Model.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Date;

@RestController
@RequestMapping("/application")
public class ApplicationController {

    @Autowired
    private ApplicationMapper applicationMapper;

    /**
     * 插入新的申请记录到数据库
     * @param application 前端传入的 Application 对象
     * @return 返回操作结果
     */
    @PostMapping("/add")
    public APIResponse<String> addApplication(@RequestBody Application application) {
        try {
            // 设置 applicationTime 为当前系统时间
            application.setApplicationTime(new Date());
            // 将数据插入数据库
            int result = applicationMapper.insert(application);
            if (result > 0) {
                return APIResponse.success("申请记录添加成功");
            } else {
                return APIResponse.error(500, "申请记录添加失败");
            }
        } catch (Exception e) {
            return APIResponse.error(500, "发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取指定用户名的所有申请记录
     * @param username 用户名
     * @return 返回该用户的所有申请记录
     */
    @GetMapping("/user/{username}")
    public APIResponse<List<Application>> getApplicationsByUsername(@PathVariable String username) {
        try {
            List<Application> applications = applicationMapper.findApplicationsByUsername(username);
            if (applications != null && !applications.isEmpty()) {
                return APIResponse.success(applications);
            } else {
                return APIResponse.error(404, "未找到该用户的申请记录");
            }
        } catch (Exception e) {
            return APIResponse.error(500, "获取申请记录时发生错误: " + e.getMessage());
        }
    }

    /**
     * 管理员同意或拒绝申请
     * @param requestData 包含用户名、状态和解释的 Map 对象
     * @return 操作结果
     */
    @PostMapping("/update")
    public APIResponse<String> updateApplication(@RequestBody Map<String, String> requestData) {
        try {
            System.out.println(requestData);

            String id = requestData.get("id");
            String status = requestData.get("status");
            String explanation = requestData.get("explanation");

             //更新申请状态
            applicationMapper.updateApplicationStatus(id, status, explanation);

            return APIResponse.success("申请已更新");
        } catch (Exception e) {
            return APIResponse.error(500, "更新申请时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取所有状态为 "等待管理员审核" 的申请记录。
     * @return 返回包含所有 "等待管理员审核" 状态申请记录的 APIResponse 列表。
     */
    @GetMapping("/pending2")
    public APIResponse<List<Application>> getPending2Applications() {
        try {
            List<Application> pendingApplications = applicationMapper.findApplicationsWaiting2();
            if (pendingApplications != null && !pendingApplications.isEmpty()) {
                return APIResponse.success(pendingApplications);
            } else {
                return APIResponse.error(404, "未找到等待管理员审核的申请记录");
            }
        } catch (Exception e) {
            return APIResponse.error(500, "获取等待管理员审核的申请记录时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取所有状态为 "等待数据所有方审核" 的申请记录。
     * @return 返回包含所有 "等待数据所有方审核" 状态申请记录的 APIResponse 列表。
     */
    @GetMapping("/pending1")
    public APIResponse<List<Application>> getPending1Applications(@RequestParam("username") String username) {
        try {
            List<Application> pendingApplications = applicationMapper.findApplicationsWaiting1(username);
            if (pendingApplications != null && !pendingApplications.isEmpty()) {
                return APIResponse.success(pendingApplications);
            } else {
                return APIResponse.error(404, "未找到等待数据所有方审核的申请记录");
            }
        } catch (Exception e) {
            return APIResponse.error(500, "获取等待数据所有方审核的申请记录时发生错误: " + e.getMessage());
        }
    }


}
