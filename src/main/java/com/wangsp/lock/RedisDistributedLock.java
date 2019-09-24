package com.wangsp.lock;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public abstract class RedisDistributedLock extends AbstractDistributedLock {

    private static final int TIMEOUT = 10000;
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

        String key = lockName();
        String result = jedis.set(key, UUID.randomUUID().toString(), "NX", "PX", TIMEOUT);

        boolean isLocked = "OK".equals(result);

        //判断业务线程是否已经执行完成，如果没有就更新锁的失效时间，防止业务线程没执行完成，锁就失效了。
        if (isLocked) {
            Thread thread = Thread.currentThread();
            new Thread(() -> {
                while (thread.isAlive()) {
                    jedis.expire(key, TIMEOUT);
                    try {
                        Thread.sleep(TIMEOUT / 2);
                    } catch (InterruptedException e) {
                        log.error("The thread sleep failure");
                    }
                }
            }).start();
        }

        return isLocked;
    }

    @Override
    public void distributedUnlock() {
        Jedis jedis = (Jedis) redisConnectionFactory.getConnection().getNativeConnection();
        jedis.del(lockName());
    }
}
