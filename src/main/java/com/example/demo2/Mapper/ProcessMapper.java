package com.example.demo2.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo2.Model.Signer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProcessMapper extends BaseMapper<Signer>{
    // 根据 data_id 查找所有的签名者
    @Select("SELECT username, data_id, role FROM signer WHERE data_id = #{data_id}")
    List<Signer> findByData_id(String data_id);
}
