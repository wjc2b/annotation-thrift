package com.wjc.thrift.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author wjc
 * @date 2024-04-06 17:21
 * @desription
 */
@ConfigurationProperties(prefix = "spring.thrift.server")
public class ThriftServerProperties {

    private String serverId;
    private int port = 25000;
    private int workerQueueCapacity = 100;
    /**
     * 服务注册信息
     */
    private ThriftServerDiscoveryProperties discovery;
    private String serverModel = TServerModel.SERVER_MODEL_DEFAULT;
    private THsHaServerProperties hsHa;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ThriftServerDiscoveryProperties getDiscovery() {
        return discovery;
    }

    public void setDiscovery(ThriftServerDiscoveryProperties discovery) {
        this.discovery = discovery;
    }

    public int getWorkerQueueCapacity() {
        return workerQueueCapacity;
    }

    public void setWorkerQueueCapacity(int workerQueueCapacity) {
        this.workerQueueCapacity = workerQueueCapacity;
    }

    public String getServerModel() {
        return serverModel;
    }

    public void setServerModel(String serverModel) {
        this.serverModel = serverModel;
    }

    public THsHaServerProperties getHsHa() {
        return hsHa;
    }

    public void setHsHa(THsHaServerProperties hsHa) {
        this.hsHa = hsHa;
    }
}
