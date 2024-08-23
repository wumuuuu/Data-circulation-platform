package com.example.demo2.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo2.Model.Signer;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SignerMapper extends BaseMapper<Signer> {

    /**
     * 插入签名者信息到 signer 表
     *
     * @param username 签名者用户名
     * @param dataId   数据 ID
     * @param role     签名者角色
     */
    @Insert("INSERT INTO signer (username, data_id, role) VALUES (#{username}, #{dataId}, #{role})")
    void insertSigner(String username, String dataId, String role);

    /**
     * 在 signer 表查找用户指定数据的权限
     *
     * @param username 签名者用户名
     * @param dataId   数据 ID
     * @return 返回用户的角色权限
     */
    @Select("SELECT role FROM `signer` WHERE username = #{username} AND data_id = #{dataId}")
    String getPermissions(@Param("username") String username, @Param("dataId") String dataId);


}
