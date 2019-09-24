package com.wangsp.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private static int ticket = 10;

    private ExecutorService executor = Executors.newCachedThreadPool();

    private CountDownLatch countDownLatch = new CountDownLatch(SUMMER);

    @GetMapping("salesTicket")
    public void redisLock_2() {
        log.info("sales 100 ticket");
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
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    log.info("线程：{} 销售一张票,剩余票数：{}",Thread.currentThread(), --ticket);
                }
                orderLock.unlock();
            });
        }
    }
}
