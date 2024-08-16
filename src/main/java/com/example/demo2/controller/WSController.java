package com.example.demo2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Service;

@Service
public class WSController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastTaskUpdate(Task task) {
        messagingTemplate.convertAndSend("/topic/tasks", task);
    }
}