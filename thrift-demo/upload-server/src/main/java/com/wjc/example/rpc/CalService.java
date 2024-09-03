package com.wjc.example.rpc;

import com.wjc.iface.thrift.CalculatorService;
import com.wjc.thrift.annotaion.ThriftService;
import org.apache.thrift.TException;

/**
 * @author wjc
 * @date 2024-05-29 21:31
 * @desription
 */
@ThriftService(name = "thrift-calculator-rpc-server",version = 2.0)
public class CalService implements CalculatorService.Iface {
    @Override
    public int add(int arg1, int arg2) throws TException {
        return 65536;
    }

    @Override
    public int subtract(int arg1, int arg2) throws TException {
        return 14767;
    }

    @Override
    public int multiply(int arg1, int arg2) throws TException {
        return 0;
    }

    @Override
    public int division(int arg1, int arg2) throws TException {
        return 0;
    }
}
