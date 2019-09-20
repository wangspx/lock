package com.wangsp.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

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
    private Jedis jedis;

    /**
     * 设置redis中锁的key值
     *
     * @return 锁名称
     */
    public abstract String lockName();

    @Override
    public boolean distributedLock() {
        return jedis.setnx(lockName(), UUID.randomUUID().toString()) == 1;
    }

    @Override
    public void distributedUnlock() {
        jedis.del(lockName());
    }
}
