package com.wangsp.lock;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author spwang Created on 2019/9/19 at 17:48
 * @version 1.0.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisDistributedLockTest {

    private static final int SUMMER = 1000;

    @Resource
    private OrderLock orderLock;

    private static int ticket = 100;

    private Executor executor = Executors.newCachedThreadPool();

    private CountDownLatch countDownLatch = new CountDownLatch(SUMMER);

    @Test
    public void redisLock_1() {
        seller();
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
                seller();
            });
        }
    }

    private void seller() {
        orderLock.lock();
        log.info("剩余票数：{}", --ticket);
        orderLock.unlock();
    }
}
