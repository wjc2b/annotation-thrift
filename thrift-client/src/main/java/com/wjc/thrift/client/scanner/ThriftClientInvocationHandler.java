package com.wjc.thrift.client.scanner;

import com.ecwid.consul.v1.ConsulClient;
import com.orbitz.consul.Consul;
import com.wjc.thrift.client.cache.ThriftServiceMethodCacheManager;
import com.wjc.thrift.client.common.ThriftClientContext;
import com.wjc.thrift.client.common.ThriftServiceSignature;
import com.wjc.thrift.client.discovery.ThriftConsulServerNode;
import com.wjc.thrift.client.discovery.ThriftConsulServerNodeList;
import com.wjc.thrift.client.exception.*;
import com.wjc.thrift.client.loadbalance.IRule;
import com.wjc.thrift.client.loadbalance.RoundRobinRule;
import com.wjc.thrift.client.loadbalance.ThriftConsulServerListLoadBalancer;
import com.wjc.thrift.client.pool.TransportKeyedObjectPool;
import com.wjc.thrift.client.properties.ThriftClientPoolProperties;
import com.wjc.thrift.client.properties.ThriftClientProperties;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Objects;

/**
 * @author wjc
 * @date 2024-04-16 14:26
 * @desription JDK动态代理类。
 */public class ThriftClientInvocationHandler implements InvocationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftClientInvocationHandler.class);

    private ThriftServiceSignature serviceSignature;

    private Class<?> clientClass;
    private Class<?> beanClass;

    private Constructor<? extends TServiceClient> clientConstructor;

    private ProxyFactoryBean proxyFactoryBean;

    private static final String DISCOVERY_ADDRESS = "http://%s";
    private ThriftConsulServerListLoadBalancer loadBalancer;
    private ThriftClientProperties properties;
    private TransportKeyedObjectPool objectPool;

    public ThriftClientInvocationHandler(ThriftServiceSignature serviceSignature, Class<?> clientClass, Constructor<? extends TServiceClient> clientConstructor) throws NoSuchMethodException {
        this.serviceSignature = serviceSignature;
        this.clientClass = clientClass;
        this.clientConstructor = clientConstructor;
//        this.proxyFactoryBean = initializeProxyFactoryBean();
    }

    public ThriftClientInvocationHandler(ThriftServiceSignature serviceSignature, Class<?> clientClass, Class<?> beanClass, Constructor<? extends TServiceClient> clientConstructor) throws NoSuchMethodException {
        this.serviceSignature = serviceSignature;
        this.clientClass = clientClass;
        this.beanClass = beanClass;
        this.clientConstructor = clientConstructor;
//        this.proxyFactoryBean = initializeProxyFactoryBean();
        initFields();
    }

    private void initFields() {
        String consulAddress = ThriftClientContext.context().getRegistryAddress();
        Consul consul;
        try{
            String url = String.format(DISCOVERY_ADDRESS,consulAddress);
            consul = Consul.builder().withUrl(url).build();
        }catch (Exception e){
            throw new ThriftClientRegistryException("create consul failed!");
        }
        if (Objects.isNull(consul)) {
            throw new ThriftClientRegistryException("Unable to access consul server, address is: " + consul);
        }
        ThriftConsulServerNodeList serverNodeList = ThriftConsulServerNodeList.singleton(consul);
        IRule roundRobinRule = new RoundRobinRule();
        this.loadBalancer = new ThriftConsulServerListLoadBalancer(serverNodeList,roundRobinRule);
        roundRobinRule.setLoadBalancer(loadBalancer);
    }

//    private ProxyFactoryBean initializeProxyFactoryBean() throws NoSuchMethodException {
//        Constructor<?> constructor = clientConstructor;
//        if (Objects.isNull(constructor)){
//            //
//            constructor = clientClass.getConstructor(TProtocol.class);
//        }
//        Object target = BeanUtils.instantiateClass(constructor, (TProtocol) null);
//        ProxyFactoryBean factoryBean = new ProxyFactoryBean();
//        factoryBean.setTarget(target);
//        factoryBean.setBeanClassLoader(getClass().getClassLoader());
//
////        ProxyFactoryBean factoryBean = new ProxyFactoryBean();
////        factoryBean.setTarget(beanClass);
////        factoryBean.setBeanClassLoader(beanClass.getClassLoader());
//        ThriftClientAdvice clientAdvice = new ThriftClientAdvice(serviceSignature, clientConstructor);
//        factoryBean.addAdvice(clientAdvice);
//        factoryBean.setSingleton(true);
//        factoryBean.setOptimize(true);
//        factoryBean.setFrozen(true);
//        return factoryBean;
//    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        System.out.println("代理的对象是："+method);
        // 对Client进行代理的结果
//        Object object = proxyFactoryBean.getObject();
        if (Objects.isNull(properties)){
            this.properties = ThriftClientContext.context().getProperties();
        }
        if (Objects.isNull(objectPool)){
            this.objectPool = ThriftClientContext.context().getObjectPool();
        }
        ThriftClientPoolProperties poolProperties = properties.getPool();
        String serviceId = serviceSignature.getThriftServiceId();
        ThriftConsulServerNode serverNode = loadBalancer.chooseServerNode(serviceId);
        String signature = serviceSignature.marker();
        int retryTimes = 0;

        TTransport transport = null;
        while(true){
            if (retryTimes++ > poolProperties.getRetryTimes()){
                LOGGER.error(
                        "All thrift client call failed, method is {}, args is {}, retryTimes: {}",
                        method.getName(), args, retryTimes);
                throw new ThriftClientException("Thrift client call failed, thrift client signature is: " + serviceSignature.marker());

            }

            try{
                transport = objectPool.borrowObject(serverNode);
                TProtocol protocol = new TCompactProtocol(transport);
                TMultiplexedProtocol tMultiplexedProtocol = new TMultiplexedProtocol(protocol, signature);
                Object client = clientConstructor.newInstance(tMultiplexedProtocol);
                // 方法缓存 暂时用不到
//                Method cachedMethod = ThriftServiceMethodCacheManager.getMethod(client.getClass(),
//                        method.getName(),
//                        method.getParameterTypes());
                // method, target, args
                // 尝试在 target 上调用方法 method , args 是方法的参数。
                return ReflectionUtils.invokeMethod(method, client, args);
            }catch (IllegalArgumentException | IllegalAccessException | InstantiationException | SecurityException | NoSuchMethodException e) {
                throw new ThriftClientOpenException("Unable to open thrift client", e);

            } catch (UndeclaredThrowableException e) {

                Throwable undeclaredThrowable = e.getUndeclaredThrowable();
                if (undeclaredThrowable instanceof TTransportException) {
                    TTransportException innerException = (TTransportException) e.getUndeclaredThrowable();
                    Throwable realException = innerException.getCause();

                    if (realException instanceof SocketTimeoutException) { // 超时,直接抛出异常,不进行重试
                        if (transport != null) {
                            transport.close();
                        }

                        LOGGER.error("Thrift client request timeout, ip is {}, port is {}, timeout is {}, method is {}, args is {}",
                                serverNode.getHost(), serverNode.getPort(), serverNode.getTimeout(),
                                method.getName(), args);
                        throw new ThriftClientRequestTimeoutException("Thrift client request timeout", e);

                    } else if (realException == null && innerException.getType() == TTransportException.END_OF_FILE) {
                        // 服务端直接抛出了异常 or 服务端在被调用的过程中被关闭了
                        objectPool.clear(serverNode); // 把以前的对象池进行销毁
                        if (transport != null) {
                            transport.close();
                        }

                    } else if (realException instanceof SocketException) {
                        objectPool.clear(serverNode);
                        if (transport != null) {
                            transport.close();
                        }
                    }

                } else if (undeclaredThrowable instanceof TApplicationException) {  // 有可能服务端返回的结果里存在null
                    LOGGER.error(
                            "Thrift end of file, ip is {}, port is {}, timeout is {}, method is {}, args is {}, retryTimes is {}",
                            serverNode.getHost(), serverNode.getPort(), serverNode.getTimeout(),
                            method.getName(), args, retryTimes);
                    if (retryTimes >= poolProperties.getRetryTimes()) {
                        throw new ThriftApplicationException("Thrift end of file", e);
                    }

                    objectPool.clear(serverNode);
                    if (transport != null) {
                        transport.close();
                    }

                } else if (undeclaredThrowable instanceof TException) { // idl exception
                    throw undeclaredThrowable;
                } else {
                    // Unknown Exception
                    throw e;
                }

            } catch (Exception e) {
                if (e instanceof ThriftClientOpenException) { // 创建连接失败
                    Throwable realCause = e.getCause().getCause();
                    // unreachable, reset router
                    if (realCause instanceof SocketException && realCause.getMessage().contains("Network is unreachable")) {
                        throw e;
                    }
                } else {
                    throw e;
                }
            } finally {
                try {
                    if (objectPool != null && transport != null) {
                        objectPool.returnObject(serverNode, transport);
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }
}

