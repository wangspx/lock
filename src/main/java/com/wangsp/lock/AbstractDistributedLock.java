package com.wangsp.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 分布式锁
 *
 * @author spwang Created on 2019/9/19 at 16:07
 * @version 1.0.0
 */
@Slf4j
public abstract class AbstractDistributedLock implements Lock {

    private static final int SLEEP_TIME = 3000;

    /**
     * 分布式锁加锁过程实现逻辑
     *
     * @return 布尔值，true：加锁成功，false：加锁失败
     */
    public abstract boolean distributedLock();

    /**
     * 布式锁解锁过程实现逻辑
     */
    public abstract void distributedUnlock();

    @Override
    public void lock() {
        log.debug("the thread [{}], try to acquire a lock", Thread.currentThread().getName());
        if (tryLock()) {
            log.debug("the thread [{}], get the lock successfully", Thread.currentThread().getName());
            return;
        }

        log.debug("the thread [{}], get the lock Failed", Thread.currentThread().getName());

        try {
            log.debug("thread [{}] wait {} ms", Thread.currentThread().getName(), SLEEP_TIME);
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
    }

    @Override
    public boolean tryLock() {
        return distributedLock();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        long endTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(time, unit);

        while (System.currentTimeMillis() <= endTime) {
            boolean b = tryLock();
            if (b) {
                return true;
            }
            Thread.sleep(SLEEP_TIME);
        }

        return false;
    }

    @Override
    public void unlock() {
        distributedUnlock();
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
