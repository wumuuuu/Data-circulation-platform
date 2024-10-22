package com.example.demo.Model;

public class Handle {
    private Integer taskId;
    private String fileId;
    private String taskType;
    private String status;
    private String d;
    private String b;
    private String y;
    private java.sql.Timestamp completedAt;

    // 默认构造函数
    public Handle() {
    }

    // 带参数的构造函数
    public Handle(Integer taskId, String fileId, String taskType, String status,
                  String d, String b, String y,
                  java.sql.Timestamp completedAt) {
        this.taskId = taskId;
        this.fileId = fileId;
        this.taskType = taskType;
        this.status = status;
        this.d = d;
        this.b = b;
        this.y = y;
        this.completedAt = completedAt;
    }

    // Getter 和 Setter 方法
    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public java.sql.Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(java.sql.Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    // toString 方法
    @Override
    public String toString() {
        return "Handle{" +
                "taskId=" + taskId +
                ", fileId='" + fileId + '\'' +
                ", taskType='" + taskType + '\'' +
                ", status='" + status + '\'' +
                ", d='" + d + '\'' +
                ", b='" + b + '\'' +
                ", y='" + y + '\'' +
                ", completedAt=" + completedAt +
                '}';
    }
}
