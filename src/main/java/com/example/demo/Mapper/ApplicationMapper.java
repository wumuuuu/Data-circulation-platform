package com.example.demo.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.Model.Application;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ApplicationMapper extends BaseMapper<Application>{

    @Insert("INSERT INTO application (username, applicationType, status, dataUser, text, startDate, endDate, applicationTime) VALUES (#{username}, #{applicationType}, #{status}, #{dataUser}, #{text}, #{startDate}, #{endDate}, #{applicationTime})")
    int insert(Application Application);

    @Select("SELECT * FROM application WHERE username = #{username}")
    List<Application> findApplicationsByUsername(@Param("username") String username);
}
