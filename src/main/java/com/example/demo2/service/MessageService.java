package com.example.demo2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void storeMessage(String username, String message) {
        redisTemplate.opsForList().rightPush("messages:" + username, message);
    }

    public List<String> getMessages(String username) {
        return redisTemplate.opsForList().range("messages:" + username, 0, -1);
    }

    public void clearMessages(String username) {
        redisTemplate.delete("messages:" + username);
    }
}
