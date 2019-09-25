package com.wangsp.lock;

/**
 * 分布式锁对外接口
 *
 * @author spwang Created on 2019/9/25 at 9:05
 * @version 1.0.0
 */
public interface DLock {

    /**
     * 获取分布式锁
     *
     * @param lockName 分布式锁的名称
     * @return 返回分布式锁的实例
     */
    AbstractDistributedLock getLock(String lockName);
}
