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
        Jedis jedis = (Jedis) redisConnectionFactory.getConnection().getNativeConnection();
        return "OK".equals(jedis.set(lockName(), UUID.randomUUID().toString(),"NX","PX",1000));
    }

    @Override
    public void distributedUnlock() {
        Jedis jedis = (Jedis) redisConnectionFactory.getConnection().getNativeConnection();
        jedis.del(lockName());
    }
}
