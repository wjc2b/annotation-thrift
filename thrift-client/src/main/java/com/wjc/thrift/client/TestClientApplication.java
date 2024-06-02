package com.wjc.thrift.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author wjc
 * @date 2024-04-18 13:54
 * @desription
 */
@SpringBootApplication
public class TestClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestClientApplication.class,args);
    }
}
