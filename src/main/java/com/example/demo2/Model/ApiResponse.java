package com.example.demo2.Model;

public class ApiResponse {
    private int code;
    private String msg;

    public ApiResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    // getters and setters
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
