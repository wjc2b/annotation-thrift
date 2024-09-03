package com.wjc.example;

import com.wjc.thrift.annotaion.EnableThriftServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author wjc
 * @date 2024-04-13 21:22
 * @desription
 */
@SpringBootApplication
@EnableThriftServer
public class ServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class,args);
    }
}
