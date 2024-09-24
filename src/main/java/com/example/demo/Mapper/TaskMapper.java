package com.example.demo.Mapper;

import com.example.demo.Model.Task;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectKey;

public interface TaskMapper {

    @SelectKey(statement = "SELECT TASK_SEQ.NEXTVAL FROM DUAL", keyProperty = "taskId", before = true, resultType = Integer.class)
    @Insert("INSERT INTO task (task_id, task_type, created_at, status, fileId) " +
            "VALUES (#{taskId}, #{taskType}, #{createdAt}, #{status}, #{fileId})")
    int insert(Task task);
}

