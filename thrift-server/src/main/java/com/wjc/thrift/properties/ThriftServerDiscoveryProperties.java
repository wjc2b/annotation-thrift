package com.wjc.thrift.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wjc
 * @date 2024-04-10 22:42
 * @desription 注册中心相关
 */
@ConfigurationProperties("spring.thrift.server.discovery")
public class ThriftServerDiscoveryProperties {

    private Boolean enabled = false;

    /**
     * 注册地址
     */
    private String host = "124.70.223.23";

    private Integer port = 8500;

    private List<String> tags = new ArrayList<>();

    private ThriftServerHealthCheckProperties healthCheck;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public ThriftServerHealthCheckProperties getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(ThriftServerHealthCheckProperties healthCheck) {
        this.healthCheck = healthCheck;
    }
}
