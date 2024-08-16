package com.example.demo2.controller;

import com.example.demo2.Mapper.ApplicationMapper;
import com.example.demo2.Model.ApiResponse;
import com.example.demo2.Model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 处理与申请相关的请求
 * 基础 URL 为 /application
 */
@RestController
@RequestMapping("/application")
public class ApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    @Autowired
    private ApplicationMapper applicationMapper;

    /**
     * 获取所有状态为 "PENDING" 的申请
     * 处理 HTTP GET 请求，URL 为 /application/pending
     * @return 返回 ApiResponse 对象，包含所有 "PENDING" 状态的申请列表
     */
    @GetMapping("/pending")
    public ApiResponse getAllApplications() {
        try {
            List<Application> applications = applicationMapper.getPendingApplications();
            return new ApiResponse(200, "Success", applications);
        } catch (Exception e) {
            log.error("Error while fetching pending applications", e);
            return new ApiResponse(500, "Failed to get pending applications", null);
        }
    }

    /**
     * 提交新的申请
     * 处理 HTTP POST 请求，URL 为 /application/submit
     * @param application 从请求体中获取 Application 对象
     * @return 返回 ApiResponse 对象，包含提交结果信息
     */
    @PostMapping("/submit")
    public ApiResponse submitApplication(@RequestBody Application application) {
        try {
            application.setApplicationTime(new Date());
            applicationMapper.insertApplication(application);
            return new ApiResponse(200, "Application submitted successfully", null);
        } catch (Exception e) {
            log.error("Error while submitting application", e);
            return new ApiResponse(500, "Failed to submit application", null);
        }
    }

    /**
     * 更新申请状态
     * 处理 HTTP POST 请求，URL 为 /application/updateApplication
     * @param application 从请求体中获取 Application 对象，包含更新后的状态
     * @return 返回 ApiResponse 对象，包含更新结果信息
     */
    @PostMapping("/updateApplication")
    public ApiResponse approveApplication(@RequestBody Application application) {
        try {
            applicationMapper.updateApplicationStatus(application.getId(), application.getApplicationStatus());
            return new ApiResponse(200, "Application status updated successfully", null);
        } catch (Exception e) {
            log.error("Error while updating application status", e);
            return new ApiResponse(500, "Failed to update application status", null);
        }
    }

    /**
     * 获取用户提交的最近一次申请状态
     * 处理 HTTP GET 请求，URL 为 /application/status
     * @param username 从请求参数中获取用户名
     * @return 返回 ApiResponse 对象，包含最近的申请状态信息
     */
    @GetMapping("/status")
    public ApiResponse getApplicationStatus(@RequestParam String username) {
        try {
            Application application = applicationMapper.findLatestApplicationByUsername(username);
            if (application != null && application.getApplicationStatus() != null) {
                return new ApiResponse(200, "Application status retrieved successfully", application);
            } else {
                return new ApiResponse(404, "No application found for this user", null);
            }
        } catch (Exception e) {
            log.error("Error while retrieving application status", e);
            return new ApiResponse(500, "Failed to retrieve application status", null);
        }
    }
}
