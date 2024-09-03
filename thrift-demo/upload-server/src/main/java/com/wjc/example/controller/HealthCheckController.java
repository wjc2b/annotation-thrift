package com.wjc.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wjc
 * @date 2024-05-30 10:40
 * @desription  应该在25001端口上实现，而不是在这里实现/health接口
 */
@RestController
public class HealthCheckController {

    @RequestMapping(value = "/health",method = RequestMethod.GET)
    public String health(){
        return "OK";
    }
}
