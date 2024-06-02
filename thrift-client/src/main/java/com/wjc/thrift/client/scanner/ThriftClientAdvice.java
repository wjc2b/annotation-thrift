package com.wjc.thrift.client.scanner;

import com.orbitz.consul.Consul;
import com.wjc.thrift.client.cache.ThriftServiceMethodCache;
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
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
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
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Objects;

/**
 * @author wjc
 * @date 2024-04-16 14:33
 * @desription 切面，相当于被代理的方法执行时，都是调用invoke方法。
 * invoke方法做的事情：
 * 1、根据lb算法，从对象池中获取了一个ServerNode
 * 2、根据ServerNode,建立客户端client
 * 3、根据原本调用的方法的名称等信息，从方法缓存池中取到对应的方法
 * 4、通过建立的client连接调用3中的方法。
 *
 */
public class ThriftClientAdvice implements MethodInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftClientAdvice.class);
    private static final String DISCOVERY_ADDRESS = "http://%s";

    private ThriftServiceSignature serviceSignature;

    private Constructor<? extends TServiceClient> clientConstructor;

    private ThriftConsulServerListLoadBalancer loadBalancer;

    private ThriftClientProperties properties;

    private TransportKeyedObjectPool objectPool;

    public ThriftClientAdvice(ThriftServiceSignature serviceSignature, Constructor<? extends TServiceClient> clientConstructor) {
        this.serviceSignature = serviceSignature;
        this.clientConstructor = clientConstructor;

        String consulAddress = ThriftClientContext.context().getRegistryAddress();
        Consul consul;
        try {
            consul = Consul.builder().withUrl(String.format(DISCOVERY_ADDRESS,consulAddress)).build();
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

    /**
     * MethodInvocation对象包含了原始方法调用的所有详细信息，包括目标对象、方法本身、方法参数等。
     * 通过调用MethodInvocation.proceed() 执行原方法。
     * @param methodInvocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
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
        Method method = methodInvocation.getMethod();
        Object[] arguments = methodInvocation.getArguments();

        int retryTimes = 0;

        TTransport transport = null;
        while(true){
            if (retryTimes++ > poolProperties.getRetryTimes()){
                LOGGER.error(
                        "All thrift client call failed, method is {}, args is {}, retryTimes: {}",
                        methodInvocation.getMethod().getName(), arguments, retryTimes);
                throw new ThriftClientException("Thrift client call failed, thrift client signature is: " + serviceSignature.marker());

            }

            try{
                transport = objectPool.borrowObject(serverNode);
                TProtocol protocol = new TCompactProtocol(transport);
                TMultiplexedProtocol tMultiplexedProtocol = new TMultiplexedProtocol(protocol, signature);
                Object client = clientConstructor.newInstance(tMultiplexedProtocol);

                Method cachedMethod = ThriftServiceMethodCacheManager.getMethod(client.getClass(),
                        method.getName(),
                        method.getParameterTypes());
                // method, target, args
                // 相当于尝试再target上调用方法method，args是方法的参数。
                // 也就是建立了一个客户端连接，在连接调用方法method。
                return ReflectionUtils.invokeMethod(cachedMethod, client, arguments);
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
                                methodInvocation.getMethod(), arguments);
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
                            methodInvocation.getMethod(), arguments, retryTimes);
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
