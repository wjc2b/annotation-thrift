package com.wjc.thrift.client.controller;

import com.wjc.example.thrift.uploadDemo;
import com.wjc.thrift.client.annotation.MyThriftClient;
import org.apache.thrift.TException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.tools.jar.Main;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * @author wjc
 * @date 2024-09-01 10:41
 * @desription
 */
@RestController
@RequestMapping("")
public class TestController {

    @MyThriftClient(name = "UploadClient",serviceId = "thrift-upload-demo",version = 2.0)
    private uploadDemo.Iface up;

    @PostMapping("/upload")
    public String uploadFile() throws IOException, TException {
        String filePath = "F:\\rpc_test\\test.txt";
        FileInputStream fis = new FileInputStream(filePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        int index = 0;
        byte[] buffer = new byte[1024];
        while((index = bis.read(buffer))!=-1){
            up.upload(ByteBuffer.wrap(buffer));
        }
        return "finished";
    }
}
