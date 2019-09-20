package com.wangsp.lock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.JedisPool;

/**
 * @author spwang Created on 2019/9/19 at 17:48
 * @version 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisDistributedLockTest {

    @Test
    public void  redisLock_1() {
        OrderLock orderLock = new OrderLock();
        orderLock.lock();
        System.out.println("11111");
        orderLock.unlock();
    }

    class OrderLock extends RedisDistributedLock {

        @Override
        public String lockName() {
            return "lock:order";
        }
    }
}
