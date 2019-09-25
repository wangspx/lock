package com.wangsp.lock.controller;

import com.wangsp.lock.redis.OrderLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.concurrent.*;

/**
 * @author spwang Created on 2019/9/24 at 9:31
 * @version 1.0.0
 */
@Slf4j
@RestController
public class TestLockController {
    private static final int SUMMER = 100;

    @Resource
    private OrderLock orderLock;

    private ExecutorService executor = Executors.newCachedThreadPool();

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    private CountDownLatch countDownLatch = new CountDownLatch(SUMMER);

    private CyclicBarrier cyclicBarrier = new CyclicBarrier(SUMMER, () -> {
        log.info("所有的线程已经执行完了, 当前票数为：{}");
    });

    @GetMapping("salesTicket")
    public void redisLock_2() {
        Jedis jedis1 = (Jedis) redisConnectionFactory.getConnection().getNativeConnection();
        log.info("sales {} ticket", jedis1.incrBy("ticket", 10L));
        jedis1.close();
        for (int i = 0; i < SUMMER; i++) {
            executor.execute(() -> {
                countDownLatch.countDown();
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                orderLock.lock();
                Jedis jedis = (Jedis) redisConnectionFactory.getConnection().getNativeConnection();
                int ticket = Integer.parseInt(jedis.get("ticket"));
                if (ticket > 0) {
                    if (ticket == 10) {
                        try {
                            log.info("停止售票中");
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    log.info("线程：{} 销售一张票,剩余票数：{}", Thread.currentThread(), jedis.decr("ticket"));
                    if (ticket == 5) {
                        throw new RuntimeException("莫名巧妙的售票异常");
                    }
                }
                jedis.close();
                orderLock.unlock();
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
