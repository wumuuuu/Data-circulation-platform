package com.example.demo2.Model;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("signer")
public class Signer {
    private String username;
    private String data_id;
    private String role;

    // Getters and Setters
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getData_id() {
        return data_id;
    }

    public void setData_id(String data_id) {
        this.data_id = data_id;
    }
}