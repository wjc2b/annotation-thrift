package com.wjc.thrift.context;

import com.wjc.thrift.properties.ThriftServerProperties;
import com.wjc.thrift.wrapper.ThriftServiceWrapper;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author wjc
 * @date 2024-04-08 22:35
 * @desription 未实现
 */
public class TSimpleServerContext implements ContextBuilder {
    private static TSimpleServerContext serverContext;

    private TSimpleServerContext(){}

    public static TSimpleServerContext context(){
        if (Objects.isNull(serverContext)){
            serverContext = new TSimpleServerContext();
        }
        return serverContext;
    }
    @Override
    public ContextBuilder prepare() {
        return null;
    }

    @Override
    public TServer buildThriftServer(ThriftServerProperties properties, List<ThriftServiceWrapper> serviceWrapperList) throws TTransportException, IOException {
        return null;
    }
}
