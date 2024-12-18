package com.example.demo.Controller;

import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class HomeController {
    @Autowired
    private UserMapper userMapper;

    /**
     * 根据用户名模糊查询用户
     * @param username 用户名搜索关键字
     * @return 匹配的用户列表
     */
    @GetMapping("/search")
    public APIResponse<List<Map<String, String>>> searchUsers(@RequestParam String username) {
        try {
            List<Map<String, String>> users = userMapper.findUsersByUsername2(username);
            return APIResponse.success(users);
        } catch (Exception e) {
            return APIResponse.error(500, "查询失败: " + e.getMessage());
        }
    }
}
