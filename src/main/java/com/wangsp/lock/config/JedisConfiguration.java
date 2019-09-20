package com.wangsp.lock.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author spwang Created on 2019/9/19 at 17:04
 * @version 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class JedisConfiguration {
    /** redis ip地址 */
    private String host;

    /** 端口（默认：6379） */
    private int port;

    /** 连接超时时间 */
    private int timeout;

    /** redis 登录密码 */
    private String password;


    @Bean
    public JedisPool jedisPool() {
        return new JedisPool(new JedisPoolConfig(), host, port, timeout, password);
    }

    @Bean
    public Jedis jedis(@Autowired JedisPool jedisPool) {
        return jedisPool.getResource();
    }
}
