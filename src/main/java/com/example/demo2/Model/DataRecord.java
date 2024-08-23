package com.example.demo2.Model;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("Update")
public class DataRecord {
    private String dataid;
    private String signy;
    private String signb;
    private LocalDateTime usageTime;


    public String getDataid() {
        return dataid;
    }

    public void setDataid(String dataid) {
        this.dataid = dataid;
    }

    public LocalDateTime getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(LocalDateTime usageTime) {
        this.usageTime = usageTime;
    }

    public String getSignb() {
        return signb;
    }

    public void setSignb(String signb) {
        this.signb = signb;
    }

    public String getSigny() {
        return signy;
    }

    public void setSigny(String signy) {
        this.signy = signy;
    }
}
