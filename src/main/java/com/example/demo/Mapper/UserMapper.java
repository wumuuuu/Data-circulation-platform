package com.example.demo.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.Model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 返回 User 对象，如果没有找到则返回 null
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{userId}")
    User findById(String userId);

    @Insert("INSERT INTO user (username, password, public_key, role) VALUES (#{username}, #{password}, #{public_key}, #{role})")
    int insert(User user);

    // 更新用户信息
    @Update("UPDATE user SET username = #{username}, password = #{password}, role = #{role}, shared_secret = #{shared_secret} WHERE id = #{id}")
    int update(User user);
}
