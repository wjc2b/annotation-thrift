package com.wjc.thrift.argument;

import com.wjc.thrift.processor.TRegisterProcessor;
import com.wjc.thrift.processor.TRegisterProcessorFactory;
import com.wjc.thrift.properties.THsHaServerProperties;
import com.wjc.thrift.properties.ThriftServerProperties;
import com.wjc.thrift.wrapper.ThriftServiceWrapper;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wjc
 * @date 2024-04-06 22:13
 * @desription 组装Server 的Args的方法
 */
public class THsHaServerArgument extends THsHaServer.Args {

    private Map<String,ThriftServiceWrapper> processorMap = new HashMap<>();
    public THsHaServerArgument(List<ThriftServiceWrapper> serviceWrapperList, ThriftServerProperties properties) throws TTransportException {
        super(new TNonblockingServerSocket(properties.getPort()));
        transportFactory(new TFastFramedTransport.Factory(1024,1024*1024*100));
        protocolFactory(new TCompactProtocol.Factory());

        // yml配置文件中读取的
        THsHaServerProperties hsHaProperties = properties.getHsHa();
        minWorkerThreads(hsHaProperties.getMinWorkerThreads());
        maxWorkerThreads(hsHaProperties.getMaxWorkerThreads());

        executorService(createInvokerPool(properties));

        try{
            TRegisterProcessor registerProcessor = TRegisterProcessorFactory.registerProcessor(serviceWrapperList);
            processorMap.clear();
            processorMap.putAll(registerProcessor.getProcessorMetaMap());
            /**
             *  这里面 registerProcessor 实际上继承了 TMultiplexedProcessor ，这个东西实现了 TProcess 中的 process()方法。
             *  process()方法会从保存的HashMap中，根据 serviceName 得到真正的 TProcess 对象，然后让这个对象进行处理。
             *  这里的HashMap是在 TRegisterProcessorFactory.registerProcessor(serviceWrapperList); 方法中注册的。
             */
            processor(registerProcessor);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }


    }


    private ExecutorService createInvokerPool(ThriftServerProperties properties) {
        THsHaServerProperties hsHaProperties = properties.getHsHa();

        return new ThreadPoolExecutor(
                hsHaProperties.getMinWorkerThreads(),
                hsHaProperties.getMaxWorkerThreads(),
                hsHaProperties.getKeepAlivedTime(), TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(properties.getWorkerQueueCapacity()));
    }
}
