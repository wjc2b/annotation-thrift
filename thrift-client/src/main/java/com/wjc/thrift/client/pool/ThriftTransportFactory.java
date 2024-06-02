package com.wjc.thrift.client.pool;

import com.wjc.thrift.client.common.ThriftServerNode;
import com.wjc.thrift.client.exception.ThriftClientConfigException;
import com.wjc.thrift.client.properties.TServiceModel;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wjc
 * @date 2024-04-18 10:51
 * @desription 根据Service的类型，也就是服务端的类型，来创建不同的Socket用于连接。
 */
public class ThriftTransportFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftTransportFactory.class);

    private static final int CONNECT_TIMEOUT = 10;

    public static TTransport determineTTransport(String serviceModel, ThriftServerNode serverNode,
                                                 int connectTimeout) {
        TTransport transport;
        switch (serviceModel){
            case TServiceModel.SERVICE_MODEL_SIMPLE:
            case TServiceModel.SERVICE_MODEL_THREAD_POOL:
                transport = createTSocket(serviceModel,serverNode,connectTimeout);
                break;
            case TServiceModel.SERVICE_MODEL_NON_BLOCKING:
            case TServiceModel.SERVICE_MODEL_HS_HA:
            case TServiceModel.SERVICE_MODEL_THREADED_SELECTOR:
                transport = createTFramedTransport(serviceModel, serverNode, connectTimeout);
                break;
            default:
                throw new ThriftClientConfigException("Service model is configured in wrong way");
        }
        return transport;
    }

    public static TTransport determineTTransport(String serviceModel, ThriftServerNode serverNode) {
        return determineTTransport(serviceModel, serverNode, CONNECT_TIMEOUT);
    }

    private static TTransport createTFramedTransport(String serviceModel, ThriftServerNode serverNode, int connectTimeout) {
        TTransport tFastFramedTransport = new TFastFramedTransport(new TSocket(serverNode.getHost(),
                serverNode.getPort(), connectTimeout > 0 ? connectTimeout : CONNECT_TIMEOUT));

        return tFastFramedTransport;
    }

    private static TTransport createTSocket(String serviceModel, ThriftServerNode serverNode, int connectTimeout) {
        TTransport tSocket = new TSocket(serverNode.getHost(),
                serverNode.getPort(), connectTimeout > 0 ? connectTimeout : CONNECT_TIMEOUT);

        return tSocket;
    }
}
