package com.wjc.thrift.context;

import com.wjc.thrift.properties.ThriftServerProperties;
import com.wjc.thrift.wrapper.ThriftServiceWrapper;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.util.List;

/**
 * @author wjc
 * @date 2024-04-06 22:03
 * @desription
 */
public interface ContextBuilder {
    ContextBuilder prepare();

    TServer buildThriftServer(ThriftServerProperties properties,
                              List<ThriftServiceWrapper> serviceWrapperList)
        throws TTransportException, IOException;
}
