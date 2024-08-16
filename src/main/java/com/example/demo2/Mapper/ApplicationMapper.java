package com.example.demo2.Mapper;

import com.example.demo2.Model.Application;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ApplicationMapper {

    /**
     * 插入新的申请记录到数据库中
     * @param application Application 对象，包含申请的详细信息
     */
    @Insert("INSERT INTO saveapplication(username, text, startDate, endDate, applicationType, applicationTime, applicationStatus) " +
            "VALUES(#{username}, #{text}, #{startDate}, #{endDate}, #{applicationType}, #{applicationTime}, #{applicationStatus})")
    void insertApplication(Application application);

    /**
     * 根据申请 ID 更新申请状态
     * @param id 申请的唯一标识符
     * @param status 要更新的申请状态
     */
    @Update("UPDATE saveapplication SET applicationStatus = #{status} WHERE id = #{id}")
    void updateApplicationStatus(@Param("id") int id, @Param("status") String status);

    /**
     * 获取所有申请记录
     * @return 包含所有申请记录的 List
     */
    @Select("SELECT * FROM saveapplication")
    List<Application> getAllApplications();

    /**
     * 获取所有待处理的申请记录
     * @return 包含所有待处理申请记录的 List
     */
    @Select("SELECT * FROM saveapplication WHERE applicationStatus = 'PENDING'")
    List<Application> getPendingApplications();

    /**
     * 根据用户名获取最近一次提交的申请记录
     * @param username 用户名
     * @return 返回最近一次提交的 Application 对象
     */
    @Select("SELECT * FROM saveapplication WHERE username = #{username} ORDER BY applicationTime DESC LIMIT 1")
    Application findLatestApplicationByUsername(String username);

}
