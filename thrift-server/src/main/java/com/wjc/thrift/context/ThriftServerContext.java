package com.wjc.thrift.context;

import com.wjc.thrift.properties.ThriftServerProperties;
import com.wjc.thrift.wrapper.ThriftServiceWrapper;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author wjc
 * @date 2024-04-06 20:33
 * @desription
 */
public class ThriftServerContext extends AbstractThriftServerContext{

    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftServerContext.class);
    private THsHaServerContext hsHaServerContext;
    private TSimpleServerContext tSimpleServerContext;

    public ThriftServerContext(ThriftServerProperties properties, List<ThriftServiceWrapper> serviceWrappers){
        this.properties = properties;
        this.serviceWrappers = serviceWrappers;
        contextInitializing();
    }

    private void contextInitializing() {
        this.hsHaServerContext = THsHaServerContext.context();
        this.tSimpleServerContext = TSimpleServerContext.context();
    }

    @Override
    protected TServer buildTNonBlockingServer() throws TTransportException, IOException {
        return null;
    }

    @Override
    protected TServer buildTSimpleServer() throws TTransportException, IOException {
        return null;
    }

    @Override
    protected TServer buildTThreadPoolServer() throws TTransportException, IOException {
        return null;
    }

    @Override
    protected TServer buildTHsHaServer() throws TTransportException, IOException {
        LOGGER.info("Build thrift server from HsHaServerContext!");
        return hsHaServerContext.buildThriftServer(properties,serviceWrappers);
    }

    @Override
    protected TServer buildTThreadedSelectorServer() throws TTransportException, IOException {
        return null;
    }
}
