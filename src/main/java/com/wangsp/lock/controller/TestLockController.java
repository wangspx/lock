package com.wangsp.lock.controller;

import com.wangsp.lock.redis.OrderLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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

    private static int ticket = 10;

    private ExecutorService executor = Executors.newCachedThreadPool();

    private CountDownLatch countDownLatch = new CountDownLatch(SUMMER);

    private CyclicBarrier cyclicBarrier = new CyclicBarrier(SUMMER, () -> {
        log.info("所有的线程已经执行完了, 当前票数为：{}", ticket);
        ticket = 10;
    });

    @GetMapping("salesTicket")
    public void redisLock_2() {
        log.info("sales {} ticket", ticket);
        for (int i = 0; i < SUMMER; i++) {
            executor.execute(() -> {
                countDownLatch.countDown();
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                orderLock.lock();
                if (ticket > 0) {
                    if (ticket == 10) {
                        try {
                            log.info("停止售票中");
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (ticket == 5) {
                        ticket--;
                        throw new RuntimeException("莫名巧妙的售票异常");
                    }
                    ticket = ticket - 1;
                    log.info("线程：{} 销售一张票,剩余票数：{}", Thread.currentThread(), ticket);
                }
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
