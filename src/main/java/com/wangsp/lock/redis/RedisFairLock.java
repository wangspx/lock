package com.wangsp.lock.redis;

import com.wangsp.lock.AbstractDistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Semaphore;

/**
 * redis 实现分布式锁
 *
 * @author spwang Created on 2019/9/19 at 16:58
 * @version 1.0.0
 */
@Slf4j
public abstract class RedisFairLock extends AbstractDistributedLock {

    /** redis key 失效时间，不能设置过小。失效时间过小的话，业务代码还没执行，锁就失效了。 */
    private static final int TIMEOUT = 20;

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    private Thread currentLockThread;

    ThreadLocal<String> local = new ThreadLocal<>();

    /** 每次只允许一个线程去访问redis，不然锁释放的一瞬间，大量的线程去访问redis，会导致连接异常 */
    private Semaphore semaphore = new Semaphore(1, true);

    @PostConstruct
    private void initSpinlock() {
        log.debug("init spinlock");

        //判断业务线程是否已经执行完成，如果没有就更新锁的失效时间，防止业务线程没执行完成，锁就失效了。
        Jedis jedis = (Jedis) redisConnectionFactory.getConnection().getNativeConnection();

        new Thread(() -> {
            while (true) {
                if (null != currentLockThread && currentLockThread.isAlive()) {
                    log.debug("the thread {} is alive, extend the [{}] lock time", currentLockThread.getName(), lockName());
                    jedis.expire(lockName(), TIMEOUT);
                    try {
                        Thread.sleep(TIMEOUT / 2);
                    } catch (InterruptedException e) {
                        log.error("The thread {} sleep failure", currentLockThread.getName());
                    }
                }
            }
        }).start();
    }

    /**
     * 设置redis中锁的key值
     *
     * @return 锁名称
     */
    public abstract String lockName();

    @Override
    public boolean distributedLock() {
        if (null != currentLockThread && currentLockThread.isAlive()) {
            return false;
        }

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Jedis jedis = (Jedis) redisConnectionFactory.getConnection().getNativeConnection();

        String uuid = UUID.randomUUID().toString();
        String result = jedis.set(lockName(), uuid, "NX", "EX", TIMEOUT);
        jedis.close();

        semaphore.release();

        boolean isLocked = "OK".equals(result);

        if (isLocked) {
            log.debug("The thread {} has acquired the [{}] lock, uuid: {}", Thread.currentThread().getName(), lockName(), uuid);
            local.set(uuid);
            this.currentLockThread = Thread.currentThread();
        }

        return isLocked;
    }

    @Override
    public void distributedUnlock() {
        Jedis jedis = (Jedis) redisConnectionFactory.getConnection().getNativeConnection();

        String script =
                "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\r\n" +
                        "    return redis.call(\"del\",KEYS[1])\r\n" +
                        "else\r\n" +
                        "    return 0\r\n" +
                        "end";

        jedis.eval(script, Collections.singletonList(lockName()), Collections.singletonList(local.get()));
        jedis.close();

        this.currentLockThread = null;

        log.debug("The thread {} has released the [{}] lock, uuid: {}", Thread.currentThread().getName(), lockName(), local.get());
        local.remove();
    }
}
