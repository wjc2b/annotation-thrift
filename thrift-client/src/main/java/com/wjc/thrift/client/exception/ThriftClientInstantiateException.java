package com.wjc.thrift.client.exception;

/**
 * @author wjc
 * @date 2024-04-15 22:24
 * @desription
 */
public class ThriftClientInstantiateException extends RuntimeException {

    public ThriftClientInstantiateException(String message) {
        super(message);
    }

    public ThriftClientInstantiateException(String message, Throwable cause) {
        super(message, cause);
    }
}
