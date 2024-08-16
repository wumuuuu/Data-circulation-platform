package com.example.demo2.controller;

// 导入必要的类和注解
import com.example.demo2.Mapper.UserMapper;
import com.example.demo2.Model.ApiResponse;
import com.example.demo2.Model.User;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController // 标记这个类为一个 Spring MVC 控制器，能够处理 HTTP 请求并返回数据
@RequestMapping("/user") // 指定这个控制器处理的基础 URL 路径为 /user
public class UserController {

    @Resource // 自动注入 UserMapper 实例，负责与数据库进行交互
    private UserMapper userMapper;

    @Autowired // 自动注入 PasswordEncoder 实例，负责加密和验证密码
    private PasswordEncoder passwordEncoder;

    /**
     * 处理用户注册请求
     * 处理 HTTP POST 请求，URL 为 /user/register
     * @param user 从请求体中获取 User 对象
     * @return 返回 ApiResponse 对象，包含注册结果信息
     */
    @PostMapping("/register")
    public ApiResponse addUser(@RequestBody User user) {
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            return new ApiResponse(1, "用户已存在", null); // 用户名已存在，返回相应信息
        }
        // 加密用户密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        return new ApiResponse(200, "注册成功", null); // 注册成功，返回相应信息
    }

    /**
     * 处理用户登录请求
     * 处理 HTTP POST 请求，URL 为 /user/login
     * @param loginData 从请求体中获取 User 对象，包含用户名和密码
     * @return 返回 ApiResponse 对象，包含登录结果信息
     */
    @PostMapping("/login")
    public ApiResponse login(@RequestBody User loginData) {
        // 根据用户名查找用户
        User user = userMapper.findByUsername(loginData.getUsername());
        if (user == null) {
            return new ApiResponse(1, "用户不存在", null); // 用户不存在
        }

        // 验证密码
        if (!passwordEncoder.matches(loginData.getPassword(), user.getPassword())) {
            return new ApiResponse(2, "密码错误", null); // 密码错误
        }

        // 登录成功
        return new ApiResponse(0, "登录成功", user.getUsername());
    }

    /**
     * 检查用户名是否存在
     * 处理 HTTP GET 请求，URL 为 /application/exists
     * @param username 从请求参数中获取用户名
     * @return 返回 ApiResponse 对象，包含用户名存在与否的信息
     */
    @GetMapping("/exists")
    public ApiResponse findByUsername(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ApiResponse(0, "用户名不能为空", null);
        }
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return new ApiResponse(1, "用户不存在", null); // 用户不存在
        }else {
            return new ApiResponse(2, "用户存在", null); // 用户存在
        }
    }

    /**
     * 处理用户查询请求
     * 处理 HTTP GET 请求，URL 为 /user
     * @param id 从请求参数中获取用户 ID
     * @return 返回根据 ID 查询到的用户对象
     */
    @GetMapping
    public User getUser(@RequestParam Integer id) {
        return userMapper.selectById(id); // 调用 UserMapper 的 selectById 方法查询用户
    }
}
