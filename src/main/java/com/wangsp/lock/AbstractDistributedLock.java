package com.wangsp.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 分布式锁
 *
 * @author spwang Created on 2019/9/19 at 16:07
 * @version 1.0.0
 */
public abstract class AbstractDistributedLock implements Lock {

    private static final int SLEEP_TIME = 100;

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
        if (tryLock()) {
            return;
        }

        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        tryLock();
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
