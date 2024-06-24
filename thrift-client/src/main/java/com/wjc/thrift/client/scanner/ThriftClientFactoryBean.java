package com.wjc.thrift.client.scanner;

import com.wjc.thrift.client.common.ThriftServiceSignature;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * @author wjc
 * @date 2024-04-16 14:00
 * @desription
 */
@Getter
@Setter
public class ThriftClientFactoryBean<T> implements FactoryBean<T>, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftClientFactoryBean.class);

    private String beanName;

    private Class<?> beanClass;

    private String beanClassName;

    private Class<?> serviceClass;

    private ThriftServiceSignature serviceSignature;

    private Class<?> clientClass;

    private Constructor<? extends TServiceClient> clientConstructor;

    @Override
    public T getObject() throws Exception {
        if (beanClass.isInterface()){
            LOGGER.info("Prepare to generate proxy for {} with JDK", beanClass.getName());
            ThriftClientInvocationHandler invocationHandler = new ThriftClientInvocationHandler(serviceSignature, clientClass,beanClass, clientConstructor);
            LOGGER.info("Proxy  Finished !");
            return (T) Proxy.newProxyInstance(beanClass.getClassLoader(),new Class<?>[]{beanClass},invocationHandler);
        }else{
            LOGGER.info("Prepare to generate proxy for {} with Cglib", beanClass.getName());
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(beanClass);
            enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
            enhancer.setUseFactory(true);
            // 正常情况下是在 MethodInterceptor 中的intercept内实现拦截逻辑。
            MethodInterceptor callback = (target, method, args, methodProxy) -> methodProxy.invokeSuper(target, args);
            enhancer.setCallback(callback);
            return (T) enhancer.create();
        }
    }

    @Override
    public Class<?> getObjectType() {
        if (Objects.isNull(beanClass) && StringUtils.isBlank(beanName)) {
            LOGGER.warn("Bean class is not found");
            return null;
        }
        if (Objects.nonNull(beanClass)) {
            return beanClass;
        }
        if (StringUtils.isNotBlank(beanClassName)) {
            try {
                beanClass = Class.forName(beanClassName);
                return beanClass;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            LOGGER.warn("Bean class is not found");
        }
        return null;
    }
    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("Succeed to instantiate an instance of ThriftClientFactoryBean: {}", this);
    }
}
