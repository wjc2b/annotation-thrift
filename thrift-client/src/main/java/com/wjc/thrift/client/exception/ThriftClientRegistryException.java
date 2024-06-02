package com.wjc.thrift.client.exception;

/**
 * @author wjc
 * @date 2024-04-16 18:57
 * @desription
 */
public class ThriftClientRegistryException extends RuntimeException {

    public ThriftClientRegistryException(String message) {
        super(message);
    }

    public ThriftClientRegistryException(String message, Throwable t) {
        super(message, t);
    }
}
