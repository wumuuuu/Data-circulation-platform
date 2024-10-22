package com.example.demo.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.Model.File;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FileMapper extends BaseMapper<File> {

    // 插入新的文件记录
    @Insert("INSERT INTO files (file_id, file_name, file_path, usage_time, creator_name, FILE_OUTLINE) " +
            "VALUES (#{file_id}, #{file_name}, #{file_path}, #{usage_time}, #{creator_name}, #{file_outline})")
    int insert(File file);

    // 根据 creator_name 查找所有 file_name
    @Select("SELECT file_name FROM files WHERE creator_name = #{creator_name}")
    List<String> findFileByCreatorName(String creator_name);

}
