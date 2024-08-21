package com.example.demo2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * 配置 RedisTemplate，用于执行 Redis 操作。
     * @param connectionFactory Redis 连接工厂，由 Spring 自动配置。
     * @return RedisTemplate 实例，用于 Redis 操作。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 设置键的序列化方式为 String
        template.setKeySerializer(new StringRedisSerializer());

        // 设置值的序列化方式为 String
        template.setValueSerializer(new StringRedisSerializer());

        // 设置哈希键的序列化方式为 String
        template.setHashKeySerializer(new StringRedisSerializer());

        // 设置哈希值的序列化方式为 String
        template.setHashValueSerializer(new StringRedisSerializer());

        return template;
    }
}


