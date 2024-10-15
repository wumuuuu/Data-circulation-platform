package com.example.demo.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.Model.Application;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ApplicationMapper extends BaseMapper<Application> {

    // 插入新的申请记录
    @Insert("INSERT INTO application (username, applicationType, status, dataUser, text, explanation, startDate, endDate, applicationTime) VALUES (#{username}, #{applicationType}, #{status}, #{dataUser}, #{text}, #{explanation}, #{startDate}, #{endDate}, #{applicationTime})")
    int insert(Application application);

    // 根据用户名查找申请记录
    @Select("SELECT * FROM application WHERE username = #{username}")
    List<Application> findApplicationsByUsername(@Param("username") String username);

    // 更新申请状态
    @Update("UPDATE application SET status = #{status}, explanation = #{explanation} WHERE id = #{id}")
    void updateApplicationStatus(@Param("id") String id, @Param("status") String status, @Param("explanation") String explanation);

    // 查找所有状态为 "等待管理员审核" 的申请记录
    @Select("SELECT * FROM application WHERE status = '等待管理员审核'")
    List<Application> findApplicationsWaiting2();

    // 查找所有状态为数据管理员是username的"等待数据所有方审核" 的申请记录
    @Select("SELECT * FROM application WHERE status = '等待数据所有方审核' AND dataUser = #{username}")
    List<Application> findApplicationsWaiting1(@Param("username") String username);
}
