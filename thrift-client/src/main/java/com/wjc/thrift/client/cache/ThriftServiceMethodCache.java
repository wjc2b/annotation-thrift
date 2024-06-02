package com.wjc.thrift.client.cache;

import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wjc
 * @date 2024-04-18 9:03
 * @desription 该条件语句用于筛查方法是否为 **非静态** 的 **public** 方法，且当前方法 **不可访问 **。
 * 将这些方法以：methodName+argName的组合存到Map中
 */
public class ThriftServiceMethodCache {

    private Map<String, Method> methodCachedMap = Maps.newHashMap();

    private final Class<?> cacheClass;

    public ThriftServiceMethodCache(Class<?> cacheClass) {
        this.cacheClass = cacheClass;
        Method[] declaredMethods = cacheClass.getDeclaredMethods();
        List<String> nonCachedMethods = new ArrayList<>();

        for (Method method:declaredMethods){
            // 该条件语句用于筛查方法是否为非静态的共有方法，且当前方法不可访问。
            // method.getModifiers() 1: 表示公有方法 ;这里找出那些属于public修饰的。
            // method.getModifiers() 8: 表示静态方法 ;这里找出那些非静态方法的
            // 当前可不可以访问，通过访问修饰符控制。
            // TODO: 但是public不应该都是可以访问的吗？
            if (!method.isAccessible() &&((1 & method.getModifiers())>0)&& ((8 & method.getModifiers()) == 0)){
                put(method);
                nonCachedMethods.add(method.getName());
            }
        }
        // 通过put(method)方法，将缓存的键更新为方法名+参数列表的形式。
        for (String methodName : nonCachedMethods) {
            // 把原始的方法名给删除。
            methodCachedMap.remove(methodName);
        }
    }

    private void put(Method method) {
        Type[] types = method.getParameterTypes();
        StringBuilder cachedKey = new StringBuilder(method.getName() + types.length);
        for (Type type : types) {
            String typeName = type.toString();
            if (typeName.startsWith("class ")) {
                typeName = typeName.substring(6);
            }
            cachedKey.append(typeName);
        }
        methodCachedMap.put(cachedKey.toString(), method);
    }

    // 根据名字或者名字+参数列表的形式从缓存中拿到方法。
    public Method getMethod(String name,Class<?>... arguments) {
        Method method = methodCachedMap.get(name);
        if (method==null){
            StringBuilder sb = new StringBuilder(name + arguments.length);
            for (Class<?> argument:arguments){
                sb.append(argument.getName());
            }
            name = sb.toString();
            method = methodCachedMap.get(name);
        }
        return method;
    }
}
