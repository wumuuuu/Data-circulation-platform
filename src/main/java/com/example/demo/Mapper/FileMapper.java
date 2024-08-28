package com.example.demo.Mapper;

import com.example.demo.Model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileMapper extends JpaRepository<FileEntity, Long> {
    // 这里你可以添加自定义的查询方法，比如根据文件名查找文件
    Optional<FileEntity> findByFileName(String fileName);
}