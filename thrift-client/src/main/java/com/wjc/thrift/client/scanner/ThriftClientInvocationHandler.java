package com.wjc.thrift.client.scanner;

import com.wjc.thrift.client.common.ThriftServiceSignature;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author wjc
 * @date 2024-04-16 14:26
 * @desription
 */public class ThriftClientInvocationHandler implements InvocationHandler {
    private ThriftServiceSignature serviceSignature;

    private Class<?> clientClass;
    private Class<?> beanClass;

    private Constructor<? extends TServiceClient> clientConstructor;

    private ProxyFactoryBean proxyFactoryBean;
    public ThriftClientInvocationHandler(ThriftServiceSignature serviceSignature, Class<?> clientClass, Constructor<? extends TServiceClient> clientConstructor) throws NoSuchMethodException {
        this.serviceSignature = serviceSignature;
        this.clientClass = clientClass;
        this.clientConstructor = clientConstructor;
        this.proxyFactoryBean = initializeProxyFactoryBean();
    }

    public ThriftClientInvocationHandler(ThriftServiceSignature serviceSignature, Class<?> clientClass, Class<?> beanClass, Constructor<? extends TServiceClient> clientConstructor) throws NoSuchMethodException {
        this.serviceSignature = serviceSignature;
        this.clientClass = clientClass;
        this.beanClass = beanClass;
        this.clientConstructor = clientConstructor;
        this.proxyFactoryBean = initializeProxyFactoryBean();
    }

    private ProxyFactoryBean initializeProxyFactoryBean() throws NoSuchMethodException {
        Constructor<?> constructor = clientConstructor;
        if (Objects.isNull(constructor)){
            //
            constructor = clientClass.getConstructor(TProtocol.class);
        }
        Object target = BeanUtils.instantiateClass(constructor, (TProtocol) null);
        ProxyFactoryBean factoryBean = new ProxyFactoryBean();
        factoryBean.setTarget(target);
        factoryBean.setBeanClassLoader(getClass().getClassLoader());

//        ProxyFactoryBean factoryBean = new ProxyFactoryBean();
//        factoryBean.setTarget(beanClass);
//        factoryBean.setBeanClassLoader(beanClass.getClassLoader());
        ThriftClientAdvice clientAdvice = new ThriftClientAdvice(serviceSignature, clientConstructor);
        factoryBean.addAdvice(clientAdvice);
        factoryBean.setSingleton(true);
        factoryBean.setOptimize(true);
        factoryBean.setFrozen(true);
        return factoryBean;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("代理的对象是："+method);
        // 对Client进行代理的结果
        Object object = proxyFactoryBean.getObject();
        // 如何调用Client中的method呢？
        return ReflectionUtils.invokeMethod(method, object, args);
    }
}

