package com.example.demo2.controller;

import com.example.demo2.Mapper.ProcessMapper;
import com.example.demo2.Model.Signer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/process")
public class ProcessController {

    @Autowired
    private ProcessMapper signerMapper;

    // 直接调用 SignerMapper 查询历史签名者并返回给前端
    @GetMapping("/getSigners")
    public List<Signer> getSigners(@RequestParam String data_id) {
        // 直接调用 MyBatis Mapper 查询
        return signerMapper.findByData_id(data_id);
    }
}
