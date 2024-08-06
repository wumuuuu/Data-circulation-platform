package com.example.demo2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
@RestController
@RequestMapping("/hello")
public class HelloController {
    @GetMapping("/all")
    public String hello() {return "hello";}
}