package com.example.demo.Model;

import java.util.Date;

public class Application {

    private int id;
    private String username;
    private String applicationType;
    private String auditStatus;
    private String processStatus;
    private Date startDate;
    private Date endDate;
    private Date applicationTime;

    // 构造函数
    public Application() {
    }

    public Application(int id, String username, String applicationType, String auditStatus, String processStatus, Date startDate, Date endDate, Date applicationTime) {
        this.id = id;
        this.username = username;
        this.applicationType = applicationType;
        this.auditStatus = auditStatus;
        this.processStatus = processStatus;
        this.startDate = startDate;
        this.endDate = endDate;
        this.applicationTime = applicationTime;
    }

    // Getter 和 Setter 方法

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(String processStatus) {
        this.processStatus = processStatus;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getApplicationTime() {
        return applicationTime;
    }

    public void setApplicationTime(Date applicationTime) {
        this.applicationTime = applicationTime;
    }

    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", applicationType='" + applicationType + '\'' +
                ", auditStatus='" + auditStatus + '\'' +
                ", processStatus='" + processStatus + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", applicationTime=" + applicationTime +
                '}';
    }
}
