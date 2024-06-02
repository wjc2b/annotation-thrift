package com.wjc.thrift.wrapper;

import java.util.Objects;

/**
 * @author wjc
 * @date 2024-04-06 18:55
 * @desription
 */
public class ThriftServiceWrapper {
    private String thriftServiceName;
    private String thriftServiceSignature;
    private Class<?> type;
    private Class<?> ifaceType;
    private double version;
    private final Object thriftService;
    private final static Double DEFAULT_VERSION = 1.0;
    // constructor================
    public ThriftServiceWrapper(String thriftServiceName, Class<?> type, Object thriftService) {
        this.thriftServiceName = thriftServiceName;
        this.type = type;
        this.thriftService = thriftService;
    }

    public ThriftServiceWrapper(String thriftServiceName, Class<?> type,  Object thriftService, double version) {
        this.thriftServiceName = thriftServiceName;
        this.type = type;
        this.version = version;
        this.thriftService = thriftService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThriftServiceWrapper that = (ThriftServiceWrapper) o;
        return thriftServiceSignature!=null?thriftServiceSignature.equals(that.thriftServiceSignature):that.thriftServiceSignature==null;
    }

    @Override
    public int hashCode() {
        return thriftServiceSignature != null ? thriftServiceSignature.hashCode() : 0;
    }

    // get/set================
    public String getThriftServiceName() {
        return thriftServiceName;
    }

    public void setThriftServiceName(String thriftServiceName) {
        this.thriftServiceName = thriftServiceName;
    }

    public String getThriftServiceSignature() {
        return thriftServiceSignature;
    }

    public void setThriftServiceSignature(String thriftServiceSignature) {
        this.thriftServiceSignature = thriftServiceSignature;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Class<?> getIfaceType() {
        return ifaceType;
    }

    public void setIfaceType(Class<?> ifaceType) {
        this.ifaceType = ifaceType;
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public Object getThriftService() {
        return thriftService;
    }
}
