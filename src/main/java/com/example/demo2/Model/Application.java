package com.example.demo2.Model;

import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@TableName("saveapplication")
public class UserApplication {
    private String username;
    private String text;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endDate;
    private String applicationType;
    private Date applicationTime;

    public Date getApplicationTime() {
        return applicationTime;
    }

    @Override
    public String toString() {
        return "Application{" +
                ", username='" + username + '\'' +
                ", text='" + text + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", applicationType='" + applicationType + '\'' +
                ", applicationTime=" + applicationTime +
                '}';
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public void setApplicationTime(Date applicationTime) {
        this.applicationTime = applicationTime;
    }

    public String getType() {
        return applicationType;
    }

    public void setType(String type) {
        this.applicationType = type;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
