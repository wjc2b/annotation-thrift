package com.wjc.thrift.instance;

import java.io.Serializable;

/**
 * @author wjc
 * @date 2024-04-11 18:38
 * @desription
 */
public class MyServiceInstancePayLoad implements Serializable {
    private static final Long SerialVersionUID = 1L;

    private double version = 1.0;

    private String description;

    public MyServiceInstancePayLoad(double version, String description) {
        this.version = version;
        this.description = description;
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
