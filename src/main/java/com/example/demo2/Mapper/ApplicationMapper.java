package com.example.demo2.Mapper;

import com.example.demo2.Model.UserApplication;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserApplicationMapper {

    @Insert("INSERT INTO saveapplication(username, text, startDate, endDate, applicationType, applicationTime) VALUES(#{username}, #{text}, #{startDate}, #{endDate}, #{applicationType}, #{applicationTime})")
    void insertApplication(UserApplication application);

}
