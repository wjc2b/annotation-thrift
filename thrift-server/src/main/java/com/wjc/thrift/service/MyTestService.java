package com.wjc.thrift.service;

import com.wjc.iface.thrift.CalculatorService;
import com.wjc.thrift.annotaion.ThriftService;
import org.apache.thrift.TException;

import java.math.BigDecimal;

/**
 * @author wjc
 * @date 2024-04-13 21:49
 * @desription
 */
@ThriftService(name = "MyTestService",version = 2.0)
public class MyTestService implements CalculatorService.Iface{
    @Override
    public int add(int arg1, int arg2) throws TException {
        System.out.println("调用 add 方法");
        return arg1+arg2;
    }

    @Override
    public int subtract(int arg1, int arg2) throws TException {
        System.out.println("调用 add 方法");
        return arg1-arg2;
    }

    @Override
    public int multiply(int arg1, int arg2) throws TException {
        System.out.println("调用 multiply 方法");
        BigDecimal arg1Decimal = new BigDecimal(arg1);
        BigDecimal arg2Decimal = new BigDecimal(arg2);
        return arg1Decimal.multiply(arg2Decimal).intValue();
    }

    @Override
    public int division(int arg1, int arg2) throws TException {
        System.out.println("调用 division 方法");
        BigDecimal arg1Decimal = new BigDecimal(arg1);
        BigDecimal arg2Decimal = new BigDecimal(arg2);
        return arg1Decimal.multiply(arg2Decimal).intValue();
    }
}
