package com.example.demo.Model;

public class UpdateTaskRequest {
    private Long taskId;
    private String username;
    private String y;
    private String b;

    // Getters 和 Setters
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
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
}

