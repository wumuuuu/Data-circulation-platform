package com.example.demo2.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo2.Model.data;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DataMapper extends BaseMapper<data>{

    @Select("SELECT COUNT(*) FROM data WHERE DataID = #{dataId}")
    boolean existsDataId(String dataId);

    @Insert("INSERT INTO data (DataID, content) VALUES (#{dataId}, #{content})")
    void insertContent(String dataId, String content);
}
