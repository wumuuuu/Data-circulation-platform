package com.example.demo2.Model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * User 类表示数据库中的 user 表。
 * 使用 MyBatis-Plus 注解进行 ORM 映射。
 */
@TableName("test") // 指定数据库表名为 "test"
public class User {

    @TableId(type = IdType.AUTO) // 标记这个字段为表的主键，并且主键类型为自动增长
    private Integer id;
    private String username; // 用户名字段
    private String password; // 密码字段
    private String rePassword;
    //无参构造函数,必须存在，否则 MyBatis-Plus 无法实例化对象。
    public User() {
    }

    /**
     * 获取主键 id 的值。
     * @return id 主键值
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置主键 id 的值。
     * @param id 主键值
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取用户名的值。
     * @return username 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名的值。
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码的值。
     * @return password 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码的值。
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }


}
