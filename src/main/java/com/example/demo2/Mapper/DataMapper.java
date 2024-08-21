package com.example.demo2.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo2.Model.data;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DataMapper extends BaseMapper<data>{

    @Insert("INSERT INTO data (DataID) VALUES (#{dataId})")
    void insertData(String dataId);
}
