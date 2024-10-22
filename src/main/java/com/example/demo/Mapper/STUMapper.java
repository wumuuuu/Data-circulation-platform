package com.example.demo.Mapper;

import com.example.demo.Model.SignTaskUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface STUMapper {

    // 插入新的 task_user 记录
    @Insert("INSERT INTO signtask_user (task_id, file_id, task_type, user_name, status, B, Y, completed_at, signer_number) " +
            "VALUES (#{taskId}, #{fileId}, #{taskType}, #{userName}, #{status}, #{b}, #{y}, #{completedAt}, #{signerNumber})")
    void insertTaskUser(SignTaskUser taskUser);

    // 根据用户名查找状态为 in_progress 的所有记录
    @Select("SELECT * FROM signtask_user WHERE user_name = #{userName}")
    List<SignTaskUser> findInProgressTasksByUserName(String userName);

    // 根据 taskId 和 userName 更新 status
    @Update("UPDATE signtask_user SET status = #{status}, y = #{y}, b = #{b} " +
            "WHERE task_id = #{taskId} AND user_name = #{userName}")
    void updateStatus(int taskId, String userName, String status, String y, String b);

    // 根据 taskId 和 signerNumber 查找用户
    @Select("SELECT * FROM SIGNTASK_USER WHERE task_id = #{taskId} AND signer_number = #{signerNumber}")
    SignTaskUser findNextSigner(int taskId, int signerNumber);

    // 根据 taskId 和 username 查找 signerNumber
    @Select("SELECT signer_number FROM SIGNTASK_USER WHERE task_id = #{taskId} AND user_name = #{username}")
    int findSignerNumber(int taskId, String username);

    // 根据 taskId 查找所有的任务
    @Select("SELECT * FROM signtask_user WHERE task_id = #{taskId}")
    List<SignTaskUser> findUserNamesAndSignerNumbersByTaskId(int taskId);

}

