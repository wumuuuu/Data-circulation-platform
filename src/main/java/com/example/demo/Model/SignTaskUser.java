package com.example.demo.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

@TableName("SIGNTASK_USER")
public class SignTaskUser {

    private Integer Id; // 每个记录的唯一 ID
    private Integer taskId; // 任务 ID（关联 task 表）
    private Integer signerNumber;
    private String fileId;
    private String taskType;
    private String userName; // 用户 ID（参与者）
    private String status; // 用户在此任务中的状态（pending, in_progress, completed）
    private String b;
    private String y;
    private java.sql.Timestamp completedAt;

    // 无参构造函数
    public SignTaskUser() {
    }

    // 带参构造函数
    public SignTaskUser(Integer Id, Integer taskId, Integer signerNumber, String fileId, String taskType, String userName, String status, String b, String y, java.sql.Timestamp completedAt) {
        this.Id = Id;
        this.taskId = taskId;
        this.signerNumber = signerNumber;
        this.fileId = fileId;
        this.taskType = taskType;
        this.userName = userName;
        this.status = status;
        this.b = b;
        this.y = y;
        this.completedAt = completedAt;
    }

    // Getter 和 Setter 方法
    public Integer getSignerNumber() {
        return signerNumber;
    }

    public void setSignerNumber(Integer signerNumber) {
        this.signerNumber = signerNumber;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer Id) {
        this.Id = Id;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    @Override
    public String toString() {
        return "SignTaskUser{" +
                "Id=" + Id +
                ", taskId=" + taskId +
                ", signerNumber=" + signerNumber +
                ", fileId='" + fileId + '\'' +
                ", taskType='" + taskType + '\'' +
                ", userName='" + userName + '\'' +
                ", status='" + status + '\'' +
                ", b='" + b + '\'' +
                ", y='" + y + '\'' +
                ", completedAt=" + completedAt +
                '}';
    }

}
