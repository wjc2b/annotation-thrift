package com.wjc.thrift.processor;

import com.wjc.thrift.wrapper.ThriftServiceWrapper;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wjc
 * @date 2024-04-07 22:10
 * @desription
 */
public class TRegisterProcessor extends TMultiplexedProcessor {
    protected volatile Map<String, ThriftServiceWrapper> processorMetaMap;

    protected TRegisterProcessor(){}

    @Override
    public void registerProcessor(String serviceName, TProcessor processor) {
        super.registerProcessor(serviceName, processor);
    }

    /**
     * 防御式拷贝。假设直接返回 processorMetaMap 对象。那么别的代码就能直接修改类内部的数据。
     * 通过这种方式，就能返回另一个对象，从而保护当前对象不会被修改。
     * 一旦用户需要修改这个map，需要使用set()方法来做。
     * @return
     */
    public Map<String, ThriftServiceWrapper> getProcessorMetaMap() {
        return new HashMap<>(processorMetaMap);
    }

    public void setProcessorMetaMap(Map<String, ThriftServiceWrapper> processorMetaMap) {
        this.processorMetaMap = processorMetaMap;
    }
}
