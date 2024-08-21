package com.example.demo2.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo2.Model.Signer;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SignerMapper extends BaseMapper<Signer> {

    /**
     * 插入签名者信息到 signer 表
     * @param username 签名者用户名
     * @param dataId 数据 ID
     * @param role 签名者角色
     * @return 影响的行数
     */
    @Insert("INSERT INTO signer (username, data_id, role) VALUES (#{username}, #{dataId}, #{role})")
    int insertSigner(String username, String dataId, String role);

}
