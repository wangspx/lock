package com.wangsp.lock;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * redis 实现分布式锁
 *
 * @author spwang Created on 2019/9/19 at 16:58
 * @version 1.0.0
 */
public abstract class RedisDistributedLock extends AbstractDistributedLock {

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * 设置redis中锁的key值
     *
     * @return 锁名称
     */
    public abstract String lockName();

    @Override
    public boolean distributedLock() {
        System.out.println("111111111111111111111111");
        Jedis jedis = (Jedis) redisConnectionFactory.getConnection().getNativeConnection();
        System.out.println("222222222222222222222222");
        String result = jedis.set(lockName(), UUID.randomUUID().toString(), "NX", "PX", 10000);
        return "OK".equals(result);
    }

    @Override
    public void distributedUnlock() {
        Jedis jedis = (Jedis) redisConnectionFactory.getConnection().getNativeConnection();
        jedis.del(lockName());
    }
}
