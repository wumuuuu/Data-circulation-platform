package com.example.demo2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTestController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/redis/test")
    public String testRedis() {

        // 向 Redis 中设置一个键值对
        redisTemplate.opsForValue().set("testKey", "Hello, Redis!");

        // 返回从 Redis 中获取这个键对应的值
        return (String) redisTemplate.opsForValue().get("testKey");
    }
}