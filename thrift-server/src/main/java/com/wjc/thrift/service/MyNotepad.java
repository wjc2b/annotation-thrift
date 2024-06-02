package com.wjc.thrift.service;

import com.wjc.iface.thrift.Notepad;
import com.wjc.thrift.annotaion.ThriftService;
import org.apache.thrift.TException;

/**
 * @author wjc
 * @date 2024-05-11 14:02
 * @desription
 */
@ThriftService(name = "MyNotepad",version = 2.0)
public class MyNotepad implements Notepad.Iface {
    @Override
    public boolean writeToServer(String content) throws TException {
        System.out.println("Connect to server Succeed! write text...");
        return true;
    }

    @Override
    public String readFromServer(String fileName) throws TException {
        String result = "simulate text reading from server!";
        return result;
    }
}
