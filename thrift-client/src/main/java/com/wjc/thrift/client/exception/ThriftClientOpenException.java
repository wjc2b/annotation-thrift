package com.wjc.thrift.client.exception;

public class ThriftClientOpenException extends RuntimeException {

    public ThriftClientOpenException(String message) {
        super(message);
    }

    public ThriftClientOpenException(String message, Throwable cause) {
        super(message, cause);
    }

}
