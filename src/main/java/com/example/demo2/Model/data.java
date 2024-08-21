package com.example.demo2.Model;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("data")  // If the table name in the database is `data`
public class data {
    private String DataID;

    // Getters and setters
    public String getDataId() {
        return DataID;
    }

    public void setDataId(String DataID) {
        this.DataID = DataID;
    }
}
