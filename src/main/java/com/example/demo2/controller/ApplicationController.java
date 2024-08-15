package com.example.demo2.controller;

import com.example.demo2.Mapper.UserApplicationMapper;
import com.example.demo2.Model.ApiResponse;
import com.example.demo2.Model.UserApplication;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import jakarta.annotation.Resource;

@RestController
@RequestMapping("/application") // 定义该控制器的基础路径为 "/application"
public class UserApplicationController {

    @Resource
    private UserApplicationMapper applicationMapper; // 注入 ApplicationMapper，用于操作数据库

    /**
     * 提交申请的 API 接口，接收前端传来的申请数据并保存到数据库
     *
     * @param application 包含申请信息的对象，由前端传递
     * @return ApiResponse 返回操作的结果，包括状态码和信息
     */
    @PostMapping("/submit")
    public ApiResponse submitApplication(@RequestBody UserApplication application) {
        try {
            // 设置申请时间为当前时间
            application.setApplicationTime(new Date());

            // 将申请信息插入到数据库中
            applicationMapper.insertApplication(application);

            // 返回成功的响应
            return new ApiResponse(200, "Application submitted successfully", null);
        } catch (Exception e) {
            // 处理异常情况，返回失败的响应
            return new ApiResponse(500, "Failed to submit application", null);
        }
    }
}
