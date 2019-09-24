package com.wangsp.lock;

import com.wangsp.lock.redis.OrderLock;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

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
        Jedis jedis = new Jedis("47.107.120.53");

        jedis.set("key1","2222");
        System.out.println("获取到的值:" + jedis.get("key1"));
        System.out.println("主线程是正常执行的");

        new Thread(()->{
            try {
                System.out.println("我已经交给了线程执行了");
                Jedis jedis2 = new Jedis("47.107.120.53");
                System.out.println("为什么到这里就是不执行");
                jedis2.set("key2","4444");
                System.out.println(jedis2.get("key2"));
                jedis2.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("也没有任何异常抛出来");
            }
        }).start();
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
