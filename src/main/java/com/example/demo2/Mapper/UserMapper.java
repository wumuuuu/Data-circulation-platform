package com.example.demo2.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo2.Model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 返回 User 对象，如果没有找到则返回 null
     */
    @Select("SELECT * FROM test WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO test (username, password, public_key, role) VALUES (#{username}, #{password}, #{public_key}, #{role})")
    int insert(User user);
}
