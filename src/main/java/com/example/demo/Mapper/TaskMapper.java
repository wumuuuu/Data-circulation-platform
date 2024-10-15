package com.example.demo.Mapper;

import com.example.demo.Model.SignTaskUser;
import com.example.demo.Model.Task;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface TaskMapper {

    @SelectKey(statement = "SELECT TASK_SEQ.NEXTVAL FROM DUAL", keyProperty = "taskId", before = true, resultType = Integer.class)
    @Insert("INSERT INTO task (task_id, task_type, created_at, status, fileId, y, b, x) " +
            "VALUES (#{taskId}, #{taskType}, #{createdAt}, #{status}, #{fileId}, #{y}, #{b}, #{x})")
    int insert(Task task);

    // 根据 taskId 修改 status y b
    @Update("UPDATE task SET status = #{status}, Y = #{y}, B = #{b} WHERE task_id = #{taskId}")
    void updateTaskFields(int taskId, String status, String y, String b);

    // 根据 taskId 修改 status y b
    @Update("UPDATE task SET X = #{x} WHERE task_id = #{taskId}")
    void updateTaskField(int taskId, String x);

    // 根据 taskId 查找任务
    @Select("SELECT * FROM task WHERE task_id = #{taskId}")
    int findTaskById(int taskId);

}
