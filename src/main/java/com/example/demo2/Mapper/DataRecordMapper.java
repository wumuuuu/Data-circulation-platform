package com.example.demo2.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UpdateMapper extends BaseMapper<Update> {

    /**
     * 插入签名参数 y 和 b 到 update 表
     *
     * @param dataId 数据 ID
     * @param signy  参数 y
     * @param signb  参数 b
     */
    @Insert("INSERT INTO `update` (dataid, signy, signb, usage_time) VALUES (#{dataId}, #{signy}, #{signb}, NOW())")
    void insertUpdate(String dataId, String signy, String signb);

    @Select("SELECT * FROM update")
    List<Update> getAllRecords();

}
