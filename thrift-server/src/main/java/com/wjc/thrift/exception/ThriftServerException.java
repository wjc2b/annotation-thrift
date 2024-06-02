package com.wjc.thrift.exception;


public class ThriftServerException extends RuntimeException {

    public ThriftServerException(String message) {
        super(message);
    }

    public ThriftServerException(String message, Throwable t) {
        super(message, t);
    }
}
