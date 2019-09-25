package com.wangsp.lock.redis;

/**
 * @author spwang Created on 2019/9/25 at 8:03
 * @version 1.0.0
 */
public abstract class RedisReentrantLock extends RedisFairLock {

    @Override
    public boolean distributedLock() {
        if (local.get() != null) {
            return true;
        }
        return super.distributedLock();
    }
}
