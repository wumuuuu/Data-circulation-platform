package com.example.demo.Model;

import java.util.Objects;

public class TaskRequest {
    private Integer taskId;
    private String username;
    private String y;
    private String b;
    private String d;

    // 默认构造函数
    public TaskRequest() {
    }

    // 带参数的构造函数
    public TaskRequest(Integer taskId, String username, String y, String b, String d) {
        this.taskId = taskId;
        this.username = username;
        this.y = y;
        this.b = b;
        this.d = d;
    }

    // Getters 和 Setters
    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    @Override
    public String toString() {
        return "TaskRequest{" +
                "taskId=" + taskId +
                ", username='" + username + '\'' +
                ", y='" + y + '\'' +
                ", b='" + b + '\'' +
                ", d='" + d + '\'' +
                '}';
    }
}
