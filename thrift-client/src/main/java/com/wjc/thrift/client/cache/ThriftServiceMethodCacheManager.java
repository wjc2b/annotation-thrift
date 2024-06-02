package com.wjc.thrift.client.cache;

import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author wjc
 * @date 2024-04-18 9:34
 * @desription 缓存管理，不懂
 */
public class ThriftServiceMethodCacheManager {
    private static final Map<String, ThriftServiceMethodCache> methodCachedMap = Maps.newConcurrentMap();

    private ThriftServiceMethodCacheManager() {
    }

    public static Method getMethod(Class<?> targetClass,String methodName,Class<?>... arguments){
        ThriftServiceMethodCache methodCache = putIfAbsent(targetClass);
        return methodCache.getMethod(methodName,arguments);
    }

    public static void put(Class<?> targetClass) {
        ThriftServiceMethodCache methodCache = new ThriftServiceMethodCache(targetClass);
        methodCachedMap.put(targetClass.getName(), methodCache);
    }

    private static ThriftServiceMethodCache putIfAbsent(Class<?> targetClass) {
        String name = targetClass.getName();
        ThriftServiceMethodCache methodCache = methodCachedMap.get(name);
        if (methodCache==null){
            methodCache = new ThriftServiceMethodCache(targetClass);
            ThriftServiceMethodCache serviceMethodCache = methodCachedMap.putIfAbsent(name, methodCache);
            if (serviceMethodCache!=null){
                methodCache  = serviceMethodCache;
            }
        }
        return methodCache;
    }

}
