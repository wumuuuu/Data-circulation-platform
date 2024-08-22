package com.example.demo2.Model;

import java.sql.Date;

public class Updata {
    private String dataid;
    private String signy;
    private String signb;
    private Date usageTime;

    public String getDataid() {
        return dataid;
    }

    public void setDataid(String dataid) {
        this.dataid = dataid;
    }

    public Date getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(Date usageTime) {
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
