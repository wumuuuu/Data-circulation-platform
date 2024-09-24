package com.example.demo.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.Model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 返回 User 对象，如果没有找到则返回 null
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM users WHERE id = #{userId}")
    User findById(String userId);

    @Insert("INSERT INTO users (username, password, public_key, role) VALUES (#{username}, #{password}, #{public_key}, #{role})")
    int insert(User user);

    // 更新用户信息
    @Update("UPDATE users SET username = #{username}, password = #{password}, role = #{role}, shared_secret = #{shared_secret} WHERE id = #{id}")
    void update(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(String id);

    @Select("SELECT id, username, role FROM users")
    List<User> findAllUsers();

    @Select("SELECT username FROM users WHERE role = '数据所有方'")
    List<User> findAllDataOwners();

}
