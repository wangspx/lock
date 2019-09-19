package com.wangsp.lock;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @author spwang Created on 2019/9/19 at 16:58
 * @version 1.0.0
 */
public abstract class RedisDistributedLock extends AbstractDistributedLock {

    @Resource
    private Jedis jedis;

    public abstract String lockName();

    @Override
    public boolean distributedLock() {
        return jedis.setnx(lockName(), UUID.randomUUID().toString()) == 0;
    }

    @Override
    public void distributedUnlock() {

    }
}
