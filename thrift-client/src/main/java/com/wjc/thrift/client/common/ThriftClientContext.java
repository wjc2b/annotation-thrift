package com.wjc.thrift.client.common;

import com.wjc.thrift.client.pool.TransportKeyedObjectPool;
import com.wjc.thrift.client.properties.ThriftClientProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * @author wjc
 * @date 2024-04-16 15:27
 * @desription
 */
@Getter
@Setter
public class ThriftClientContext {
    private static volatile ThriftClientContext context;
    private ThriftClientProperties properties;

    private TransportKeyedObjectPool objectPool;

    private String registryAddress;
    private ThriftClientContext(){}
    public static ThriftClientContext context(){
        if (context==null){
            synchronized (ThriftClientContext.class){
                if (context == null){
                    context = new ThriftClientContext();
                }
            }
        }
        return context;
    }
    public static ThriftClientContext context(ThriftClientProperties properties, TransportKeyedObjectPool objectPool) {
        context().properties = properties;
        context().objectPool = objectPool;
        return context;
    }

    public static void registry(String registryAddress) {
        context().registryAddress = registryAddress;
    }
}
