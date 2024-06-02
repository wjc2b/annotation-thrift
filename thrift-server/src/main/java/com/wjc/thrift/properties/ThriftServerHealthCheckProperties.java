package com.wjc.thrift.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author wjc
 * @date 2024-04-10 22:54
 * @desription
 */
@ConfigurationProperties(prefix = "spring.thrift.server.discovery.health-check")
public class ThriftServerHealthCheckProperties {

    private Boolean enabled = true;

    private Long checkInterval = 20L;

    private Long checkTimeout = 3L;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(Long checkInterval) {
        this.checkInterval = checkInterval;
    }

    public Long getCheckTimeout() {
        return checkTimeout;
    }

    public void setCheckTimeout(Long checkTimeout) {
        this.checkTimeout = checkTimeout;
    }
}
