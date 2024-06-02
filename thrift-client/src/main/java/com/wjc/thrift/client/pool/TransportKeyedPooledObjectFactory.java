package com.wjc.thrift.client.pool;

import com.wjc.thrift.client.common.ThriftServerNode;
import com.wjc.thrift.client.exception.ThriftClientConfigException;
import com.wjc.thrift.client.exception.ThriftClientOpenException;
import com.wjc.thrift.client.properties.ThriftClientPoolProperties;
import com.wjc.thrift.client.properties.ThriftClientProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author wjc
 * @date 2024-04-18 10:47
 * @desription
 */
public class TransportKeyedPooledObjectFactory extends BaseKeyedPooledObjectFactory<ThriftServerNode, TTransport> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportKeyedPooledObjectFactory.class);

    private ThriftClientProperties properties;

    public TransportKeyedPooledObjectFactory(ThriftClientProperties properties) {
        this.properties = properties;
    }


    @Override
    public TTransport create(ThriftServerNode key) throws Exception {
        if (StringUtils.isBlank(key.getHost())){
            throw new ThriftClientConfigException("Invalid Thrift server, node IP address: " + key.getHost());
        }
        if (key.getPort() <= 0 || key.getPort() > 65535) {
            throw new ThriftClientConfigException("Invalid Thrift server, node port: " + key.getPort());
        }
        TTransport transport;
        // 获得客户端池的配置信息
        ThriftClientPoolProperties poolProperties = properties.getPool();
        if (Objects.isNull(poolProperties)) {
            transport = ThriftTransportFactory.determineTTransport(properties.getServiceModel(), key);
        }else{
            int connectTimeout = poolProperties.getConnectTimeout();
            if (connectTimeout>0){
                transport = ThriftTransportFactory.determineTTransport(properties.getServiceModel(), key,connectTimeout);
            }else{
                transport = ThriftTransportFactory.determineTTransport(properties.getServiceModel(), key);
            }
        }
        try {
            // 打开通道
            transport.open();
            LOGGER.info("Open a new transport {}", transport);
        } catch (TTransportException e) {
            throw new ThriftClientOpenException("Connect to " + key.getHost() + ":" + key.getPort() + " failed", e);
        }

        return transport;
    }

    @Override
    public PooledObject<TTransport> wrap(TTransport tTransport) {
        return new DefaultPooledObject<>(tTransport);
    }
    @Override
    public boolean validateObject(ThriftServerNode key, PooledObject<TTransport> value) {
        if (Objects.isNull(value)) {
            LOGGER.warn("PooledObject is already null");
            return false;
        }

        TTransport transport = value.getObject();
        if (Objects.isNull(transport)) {
            LOGGER.warn("Pooled transport is already null");
            return false;
        }

        try {
            return transport.isOpen();
        } catch (Exception e) {
            LOGGER.error(e.getCause().getMessage());
            return false;
        }
    }

    @Override
    public void destroyObject(ThriftServerNode key, PooledObject<TTransport> value) throws Exception {
        if (Objects.nonNull(value)) {
            TTransport transport = value.getObject();
            if (Objects.nonNull(transport)) {
                transport.close();
            }
            value.markAbandoned();
        }
    }

}
