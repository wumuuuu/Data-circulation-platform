package com.example.demo.Controller;

import com.example.demo.Model.User;
import com.example.demo.Service.CustomUserDetailsService;
import com.example.demo.Service.ECDHService;
import com.example.demo.Model.APIResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

@RestController
public class AuthController {

    // 注入 ECDHService，用于处理密钥生成和共享密钥计算
    @Autowired
    private ECDHService dhService;

    // 注入 CustomUserDetailsService，用于处理用户相关的业务逻辑
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // 注入 PasswordEncoder，用于对密码进行加密和验证
    @Autowired
    private PasswordEncoder passwordEncoder;

    // 注入 HttpSession，用于在会话中存储和获取数据
    @Autowired
    private HttpSession session;

    @RequestMapping(value = "/exchange-keys", method = RequestMethod.OPTIONS)
    public APIResponse<String> handleOptions() {
        return APIResponse.success("1");// 直接返回 200，处理预检请求
    }
    /**
     * 处理密钥交换的 POST 请求。客户端将其公钥发送给服务器，
     * 服务器使用自己的私钥和客户端公钥生成共享密钥，并将共享密钥存储在会话中。
     *
     * @param requestBody 客户端的公钥，作为请求参数传递
     * @return 包含服务器公钥的 APIResponse 对象
     */
    @PostMapping("/exchange-keys")
    public APIResponse<String> exchangeKeys(@RequestBody Map<String, Object> requestBody) {
        try {
            // 从对象中获取 clientPublicKey 字段
            String clientPublicKey = (String) requestBody.get("clientPublicKey");
            if (clientPublicKey == null) {
                return APIResponse.error(400, "Missing clientPublicKey");
            }

            // 1. 生成服务器端的 ECDH 密钥对
            KeyPair serverKeyPair = dhService.generateKeyPair();

            // 2. 将客户端公钥字符串解码为 PublicKey 对象
            PublicKey clientPubKey = dhService.decodePublicKey(clientPublicKey);

            // 3. 使用服务器的私钥和客户端的公钥计算共享密钥
            byte[] sharedSecret = dhService.generateSharedSecret(serverKeyPair.getPrivate(), clientPubKey);

            // 4. 将共享密钥存储在会话中
            session.setAttribute("sharedSecret", sharedSecret);

            // 5. 将服务器的公钥编码为 Base64 字符串，返回给客户端
            String serverPublicKey = Base64.getEncoder().encodeToString(serverKeyPair.getPublic().getEncoded());

            return APIResponse.success(serverPublicKey);
        } catch (Exception e) {
            // 如果发生异常，返回错误响应
            return APIResponse.error(500, "密钥交换失败: " + e.getMessage());
        }
    }

    /**
     * 处理用户注册的 POST 请求。客户端将用户名和加密的密码发送给服务器，
     * 服务器使用共享密钥解密密码，并将用户信息保存到数据库中。
     *
     * @param requestBody 用户名，加密后的密码，作为请求参数传递
     * @return 注册结果的 APIResponse 对象
     */
    @PostMapping("/register")
    public APIResponse<String> registerUser(@RequestBody Map<String, Object> requestBody) {
        try {
            // 从 requestBody 提取 username 和加密后的 password
            String username = (String) requestBody.get("username");
            String encryptedPassword = (String) requestBody.get("password");

            // 1. 从会话中获取先前存储的共享密钥
            byte[] sharedSecret = (byte[]) session.getAttribute("sharedSecret");

            // 如果共享密钥不存在，返回错误响应
            if (sharedSecret == null) {
                return APIResponse.error(400, "共享密钥未找到，请重新进行密钥交换");
            }

            // 2. 使用共享密钥解密客户端发送的密码
            String decryptedPassword = dhService.decrypt(encryptedPassword, sharedSecret);

            // 3. 创建用户对象并保存到数据库
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(decryptedPassword)); // 对解密后的密码进行加密
            user.setRole((String) requestBody.get("role")); // 获取其他字段如角色
            customUserDetailsService.saveUser(user); // 保存用户到数据库

            // 4. 注册完成后，清除会话中的共享密钥
            session.removeAttribute("sharedSecret");

            return APIResponse.success("注册成功");

        } catch (Exception e) {

            return APIResponse.error(500, "注册失败: " + e.getMessage());
        }
    }

    /**
     * 处理用户登录的 POST 请求。客户端将用户名和加密的密码发送给服务器，
     * 服务器使用共享密钥解密密码，并验证用户的身份。
     *
     * @param requestBody 用户名，加密后的密码，作为请求参数传递
     * @return 登录结果的 APIResponse 对象
     */
    @PostMapping("/login")
    public APIResponse<String> loginUser(@RequestBody Map<String, Object> requestBody) {
        try {
            // 从 requestBody 提取 username 和加密后的 password
            String username = (String) requestBody.get("username");
            String encryptedPassword = (String) requestBody.get("password");

            // 从会话中获取共享密钥
            byte[] sharedSecret = (byte[]) session.getAttribute("sharedSecret");

            // 如果共享密钥不存在，返回错误响应
            if (sharedSecret == null) {
                return APIResponse.error(400, "共享密钥未找到，请重新进行密钥交换");
            }

            // 使用共享密钥解密客户端发送的密码
            String decryptedPassword = dhService.decrypt(encryptedPassword, sharedSecret);

            // 查找用户并验证密码
            org.springframework.security.core.userdetails.User userDetails =
                    (org.springframework.security.core.userdetails.User) customUserDetailsService.loadUserByUsername(username);

            // 如果用户存在且密码匹配，登录成功
            if (userDetails != null && passwordEncoder.matches(decryptedPassword, userDetails.getPassword())) {

                String encodedSecret = Base64.getEncoder().encodeToString(sharedSecret);

                // 存储共享密钥以备将来使用
                customUserDetailsService.storeSharedSecret(userDetails.getUsername(), encodedSecret);

                return APIResponse.success("登录成功");
            } else {
                // 用户名或密码错误，返回未授权响应
                return APIResponse.error(401, "用户名或密码错误");
            }
        } catch (Exception e) {
            // 如果发生异常，返回错误响应
            return APIResponse.error(500, "登录失败: " + e.getMessage());
        }
    }
}
