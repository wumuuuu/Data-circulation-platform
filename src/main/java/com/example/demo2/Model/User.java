package com.example.demo2.Model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * User 类表示数据库中的 user 表。
 * 使用 MyBatis-Plus 注解进行 ORM 映射。
 */
@TableName("test") // 指定数据库表名为 "test"
public class User {

    @TableId(type = IdType.AUTO) // 标记这个字段为表的主键，并且主键类型为自动增长
    private String username;
    private String password;
    private String rePassword;
    @TableField("public_key") // 映射数据库字段
    private String public_key;
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // getter 和 setter 方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPublicKey() {
        return public_key;
    }

    public void setPublicKey(String public_key) {
        this.public_key = public_key;
    }

    public String getPublic_key() {
        return public_key;
    }

    public void setPublic_key(String public_key) {
        this.public_key = public_key;
    }
}
