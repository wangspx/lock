package com.wangsp.lock.controller;

import com.wangsp.lock.redis.OrderLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author spwang Created on 2019/9/24 at 9:31
 * @version 1.0.0
 */
@Slf4j
@RestController
public class TestLockController {
    private static final int SUMMER = 1000;

    @Resource
    private OrderLock orderLock;

    private ExecutorService executor = Executors.newCachedThreadPool();

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    private CountDownLatch countDownLatch = new CountDownLatch(SUMMER);


    @GetMapping("salesTicket")
    public void redisLock_2() {
        int ticket1 = getTicket();
        log.info("sales {} tickets", ticket1);
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
                int ticket = getTicket();
                if (ticket > 0) {
                    if (ticket == 10) {
                        try {
                            log.info("停止售票中");
                            Thread.sleep(15000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    Transaction multi = jedis.multi();

                    Response<Long> tickets = multi.decr("tickets");

                    HashMap<String, String> hash = new HashMap<>();
                    hash.put("id", String.valueOf(tickets.get()));
                    hash.put("uuid", UUID.randomUUID().toString());
                    hash.put("create_time", String.valueOf(System.currentTimeMillis()));
                    multi.hmset("o:" + tickets.get(), hash);

                    multi.exec();

                    log.info("线程：{} 销售一张票,剩余票数：{}", Thread.currentThread(), tickets.get());
                    if (ticket == 5) {
                        throw new RuntimeException("售完票后，莫名巧妙的售票异常");
                    }
                }
                jedis.close();
                orderLock.unlock();
            });
        }
    }

    private int getTicket() {
        Jedis jedis1 = (Jedis) redisConnectionFactory.getConnection().getNativeConnection();
        String tickets = jedis1.get("tickets");
        return Integer.parseInt(tickets);
    }
}
