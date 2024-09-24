package com.example.demo.Model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

@TableName("FILES")
public class File {

    private String file_id;
    private String file_name;
    private String file_path;
    private String creator_name;
    private String file_outline;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date usage_time;

    // Getters and Setters
    public String getFile_id() {
        return file_id;
    }

    public void setFile_id(String file_id) {
        this.file_id = file_id;
    }

    public String getCreator_name() {
        return creator_name;
    }

    public void setCreator_name(String creator_name) {
        this.creator_name = creator_name;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public String getFile_outline() {
        return file_outline;
    }

    public void setFile_outline(String file_outline) {
        this.file_outline = file_outline;
    }

    public Date getUsage_time() {
        return usage_time;
    }

    public void setUsage_time(Date usage_time) {
        this.usage_time = usage_time;
    }

    public static class SignTask {
    }
}
