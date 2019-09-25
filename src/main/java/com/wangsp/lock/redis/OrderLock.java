package com.wangsp.lock.redis;

import org.springframework.stereotype.Component;

/**
 * @author spwang Created on 2019/9/20 at 9:59
 * @version 1.0.0
 */
@Component
public class OrderLock extends RedisFairLock {
    @Override
    public String lockName() {
        return "lock:order";
    }
}
