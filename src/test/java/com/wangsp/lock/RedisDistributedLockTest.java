package com.wangsp.lock;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.concurrent.*;

/**
 * @author spwang Created on 2019/9/19 at 17:48
 * @version 1.0.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisDistributedLockTest {

    private static final int SUMMER = 2;

    @Resource
    private OrderLock orderLock;

    private static int ticket = 100;

    private ExecutorService executor = Executors.newCachedThreadPool();

    private CountDownLatch countDownLatch = new CountDownLatch(SUMMER);


    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    public void redisLock_1() {
        executor.execute(() -> {
            Jedis jedis = (Jedis) redisConnectionFactory.getConnection().getNativeConnection();
            jedis.set("2222","22222");
            System.out.println(jedis.get("2222"));
        });
        seller();
    }

    @Test
    public void redisLock_3() {
        Jedis jedis = (Jedis) redisConnectionFactory.getConnection().getNativeConnection();
        jedis.set("2222","22222");
        System.out.println(jedis.get("2222"));
    }

    @Test
    public void redisLock_2() {
        for (int i = 0; i < SUMMER; i++) {
            executor.execute(() -> {
                countDownLatch.countDown();
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                orderLock.lock();
                log.info("剩余票数：{}", --ticket);
                orderLock.unlock();
            });
        }
    }

    private void seller() {

    }
}
