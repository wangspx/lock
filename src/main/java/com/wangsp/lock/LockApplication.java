package com.wangsp.lock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;

import java.sql.SQLOutput;
import java.util.List;

@SpringBootApplication
public class LockApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockApplication.class, args);

        Jedis jedis = new Jedis("47.107.120.53");

        jedis.set("key1","2222");
        System.out.println("获取到的值:" + jedis.get("key1"));
        System.out.println("主线程是正常执行的");

        new Thread(()->{
            try {
                System.out.println("我已经交给了线程执行了");
                Jedis jedis2 = new Jedis("47.107.120.53");
                System.out.println("为什么到这里就是不执行");
                jedis2.set("key2","4444");
                Response<List<String>> mget = jedis2.pipelined().mget("key1", "key2");
                mget.get().forEach(System.out::println);
                System.out.println(jedis2.get("key2"));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("也没有任何异常抛出来");
            }
        }).start();
    }

}
