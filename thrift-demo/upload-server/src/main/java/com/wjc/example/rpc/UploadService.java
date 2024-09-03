package com.wjc.example.rpc;

import com.wjc.example.thrift.uploadDemo;
import com.wjc.thrift.annotaion.ThriftService;
import org.apache.thrift.TException;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wjc
 * @date 2024-09-01 13:13
 * @desription
 */
@ThriftService(name = "thrift-upload-demo",version = 2.0)
public class UploadService implements uploadDemo.Iface{
    private static AtomicInteger a = new AtomicInteger(0);
    @Override
    public boolean upload(ByteBuffer file) throws TException, IOException {
        String filePath = "F:\\rpc_test\\getTest.txt";
        FileOutputStream fos = new FileOutputStream(filePath);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bos.write(file.array());
        System.out.println(file.array());
        bos.flush();
        return true;
    }
}
