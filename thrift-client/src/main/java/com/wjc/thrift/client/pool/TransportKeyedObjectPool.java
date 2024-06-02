package com.wjc.thrift.client.pool;

import com.wjc.thrift.client.common.ThriftServerNode;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.thrift.transport.TTransport;

/**
 * @author wjc
 * @date 2024-04-16 15:43
 * @desription
 */
public class TransportKeyedObjectPool extends GenericKeyedObjectPool<ThriftServerNode, TTransport> {

    public TransportKeyedObjectPool(KeyedPooledObjectFactory<ThriftServerNode, TTransport> factory) {
        super(factory);
    }

    public TransportKeyedObjectPool(KeyedPooledObjectFactory<ThriftServerNode, TTransport> factory, GenericKeyedObjectPoolConfig<TTransport> config) {
        super(factory, config);
    }

    @Override
    public TTransport borrowObject(ThriftServerNode key) throws Exception {
        return super.borrowObject(key);
    }

    @Override
    public void returnObject(ThriftServerNode key, TTransport obj) {
        super.returnObject(key, obj);
    }
}
