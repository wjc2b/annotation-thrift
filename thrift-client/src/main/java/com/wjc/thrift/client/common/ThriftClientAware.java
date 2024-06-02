package com.wjc.thrift.client.common;

import org.apache.thrift.TServiceClient;

/**
 * @author wjc
 * @date 2024-04-16 9:48
 * @desription
 */
public interface ThriftClientAware<T extends TServiceClient> {
    T client();
}
