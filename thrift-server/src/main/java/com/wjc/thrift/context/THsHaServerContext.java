package com.wjc.thrift.context;

import com.wjc.thrift.argument.THsHaServerArgument;
import com.wjc.thrift.properties.ThriftServerProperties;
import com.wjc.thrift.wrapper.ThriftServiceWrapper;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author wjc
 * @date 2024-04-06 22:03
 * @desription 构建一个半同步半异步的Server,填充其中的Context
 */
public class THsHaServerContext implements ContextBuilder{
    private static THsHaServerContext serverContext;
    private THsHaServer.Args args;
    private THsHaServerContext(){}

    public static THsHaServerContext context(){
        if (Objects.isNull(serverContext)){
            serverContext = new THsHaServerContext();
        }
        return serverContext;
    }

    @Override
    public ContextBuilder prepare() {
        return context();
    }

    @Override
    public TServer buildThriftServer(ThriftServerProperties properties, List<ThriftServiceWrapper> serviceWrapperList) throws TTransportException, IOException {
        serverContext = (THsHaServerContext) prepare();
        serverContext.args = new THsHaServerArgument(serviceWrapperList,properties);
        return new THsHaServer(args);
    }
}
