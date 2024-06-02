package com.wjc.thrift.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wjc
 * @date 2024-05-30 10:58
 * @desription
 */
@RestController
public class HealthController {
    @GetMapping("/health")
    public String healthCheck() {
        // 检查服务健康状况
        // ...

        return "OK"; // 返回 "OK" 表示服务健康
    }
}
