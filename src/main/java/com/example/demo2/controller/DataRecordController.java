package com.example.demo2.controller;

import com.example.demo2.Mapper.DataRecordMapper;
import com.example.demo2.Model.ApiResponse;
import com.example.demo2.Model.DataRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/data") // 正确的路径映射
public class DataRecordController {

    private static final Logger log = LoggerFactory.getLogger(DataRecordController.class);

    @Autowired
    private DataRecordMapper dataRecordMapper;

    @GetMapping("/records")
    public ApiResponse getAllRecords() {
        try {
            List<DataRecord> Records = dataRecordMapper.getAllRecords();
            return new ApiResponse(200, "Success", Records);
        } catch (Exception e) {
            log.error("Error while fetching DataRecord", e);
            return new ApiResponse(500, "Failed to get DataRecord", null);
        }
    }
}
