package com.wjc.thrift.client.exception;

/**
 * @author wjc
 * @date 2024-04-16 10:57
 * @desription
 */
public class ThriftClientConfigException extends RuntimeException {

    public ThriftClientConfigException(String message) {
        super(message);
    }

    public ThriftClientConfigException(String message, Throwable cause) {
        super(message, cause);
    }

}