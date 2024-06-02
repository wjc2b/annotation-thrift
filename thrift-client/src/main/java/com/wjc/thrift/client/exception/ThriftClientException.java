package com.wjc.thrift.client.exception;

/**
 * @author wjc
 * @date 2024-04-17 16:52
 * @desription
 */
public class ThriftClientException extends RuntimeException {

    public ThriftClientException(String message) {
        super(message);
    }

    public ThriftClientException(String message, Throwable t) {
        super(message, t);
    }
}

