package com.wjc.thrift.client.controller;

import com.wjc.iface.thrift.CalculatorService;
import com.wjc.thrift.client.annotation.MyThriftClient;
import com.wjc.thrift.client.annotation.ThriftRefer;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.apache.thrift.async.AsyncMethodCallback;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wjc
 * @date 2024-04-18 14:33
 * @desription
 */
//@RestController
//@RequestMapping("/rpc")
public class TestController {

//    @ThriftRefer
//    private CalculatorThriftClient calculator;

    // 直接开一个JDK动态代理，代理里面的接口。
    // 调用的时候找到Client,然后创建client就能访问了。
    @MyThriftClient(name = "MyCalculatorService",serviceId = "thrift-calculator-rpc-server",version = 2.0)
    private CalculatorService.Iface calcu;
//    @MyThriftClient(name = "MyCalculatorService",serviceId = "thrift-calculator-rpc-server",version = 2.0)
//    private CalculatorService.AsyncIface asyncIface;



    @GetMapping("/add")
    public int add(@RequestParam("arg1") int arg1, @RequestParam("arg2") int arg2) throws Exception {
        return calcu.add(arg1,arg2);
    }

    @GetMapping("/async_add")
    public void async_add(@RequestParam("arg1") int arg1, @RequestParam("arg2") int arg2) throws Exception {
        AsyncMethodCallback callback = new AsyncMethodCallback() {
            @Override
            public void onComplete(Object response) {
                System.out.println("********Async=========: "+response.toString());
            }

            @Override
            public void onError(Exception exception) {

            }
        };
//        asyncIface.add(arg1,arg2,callback);
    }

}
