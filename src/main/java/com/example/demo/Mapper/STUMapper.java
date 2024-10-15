package com.example.demo.Mapper;

import com.example.demo.Model.SignTaskUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface STUMapper {

    // 插入新的 task_user 记录
    @Insert("INSERT INTO signtask_user (task_id, file_id, task_type, user_name, status, B, Y, completed_at, next_signer) " +
            "VALUES (#{taskId}, #{fileId}, #{taskType}, #{userName}, #{status}, #{b}, #{y}, #{completedAt}, #{nextSigner})")
    void insertTaskUser(SignTaskUser taskUser);

    // 根据用户名查找状态为 in_progress 的所有记录
    @Select("SELECT * FROM signtask_user WHERE user_name = #{userName} AND status = 'in_progress'")
    List<SignTaskUser> findInProgressTasksByUserName(String userName);

    // 根据 taskId 和 userName 查找 nextSigner
    @Select("SELECT next_signer FROM signtask_user WHERE task_id = #{taskId} AND user_name = #{userName}")
    String findNextSigner(int taskId, String userName);

    // 根据 taskId 和 userName 更新 status
    @Update("UPDATE signtask_user SET status = #{status}, y = #{y}, b = #{b} " +
            "WHERE task_id = #{taskId} AND user_name = #{userName}")
    void updateStatus(int taskId, String userName, String status, String y, String b);
}

