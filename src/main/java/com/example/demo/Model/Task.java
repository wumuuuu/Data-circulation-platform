package com.example.demo.Model;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

@TableName("TASK")
public class Task {

    private Integer taskId;
    private String taskType;
    private String fileId;
    private java.sql.Timestamp createdAt;
    private String status;
    private String y;
    private String b;
    private String x;

    public Task() {
    }



    // 带参构造函数
    public Task(Integer taskId, String taskType, String fileId, java.sql.Timestamp createdAt, String status, String y, String b, String x) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.fileId = fileId;
        this.createdAt = createdAt;
        this.status = status;
        this.y = y;
        this.b = b;
        this.x = x;
    }

    // Getter 和 Setter 方法

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
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

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public java.sql.Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.sql.Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // toString 方法（可选）
    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", taskType='" + taskType + '\'' +
                ", fileId='" + fileId + '\'' +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                '}';
    }

}

