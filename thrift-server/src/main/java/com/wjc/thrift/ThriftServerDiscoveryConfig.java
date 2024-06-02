package com.wjc.thrift;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableRegCheck;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import com.wjc.thrift.properties.ThriftServerDiscoveryProperties;
import com.wjc.thrift.properties.ThriftServerHealthCheckProperties;
import com.wjc.thrift.properties.ThriftServerProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.Inet4Address;
import java.util.List;

/**
 * @author wjc
 * @date 2024-04-10 22:39
 * @desription
 */
@Configuration
@AutoConfigureAfter(ThriftServerAutoConfiguration.class)
public class ThriftServerDiscoveryConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftServerDiscoveryConfig.class);
    private static final String REGISTRY_URL_TEMPLATE = "http://%s:%d";
    private static final String HEALTH_CHECK_URL_TEMPLATE = "%s:%d";

    @Bean
    public ConsulClient thriftConsulClient(ThriftServerProperties thriftServerProperties) throws Exception {
        // 相当于是服务注册的信息
        ThriftServerDiscoveryProperties discoveryProperties = thriftServerProperties.getDiscovery();

        // 注册到哪个地址和端口上。相当于是Consul的地址和端口
        String discoveryHost = discoveryProperties.getHost();
//        String discoveryHost = "localhost";
        Integer discoveryPort = discoveryProperties.getPort();

        LOGGER.info("Service discovery in host:{}, port:{}",discoveryHost,discoveryPort);
        String serverName = thriftServerProperties.getServerId();
        // 获得服务所在的地址和port
        String serverHostAddress = discoveryProperties.getHost(); // 先注册到124上
//        String serviceHostAddress = "localhost"; // 先注册到124上
//        String serviceHostAddress = Inet4Address.getLocalHost().getHostAddress();
        Integer serverHostPort = thriftServerProperties.getPort();

        String serverId = String.join(":",serverName,serverHostAddress,String.valueOf(serverHostPort));
        LOGGER.info("Service id:{}, Service name :{}, Service address:{}, Service port:{}",
                serverId,serverName,serverHostAddress,serverHostPort);
        List<String> tags = discoveryProperties.getTags();
        if (CollectionUtils.isNotEmpty(tags)){
            LOGGER.info("Service tags:[{}]",String.join(",",tags));
        }
        String discoveryUrl = String.format(REGISTRY_URL_TEMPLATE,discoveryHost,discoveryPort);

        ConsulClient consulClient = new ConsulClient(discoveryHost,discoveryPort);
        registerAgentService(discoveryProperties,serverName,serverHostAddress,serverHostPort,
                serverId,tags,consulClient);
        return consulClient;
    }

    /**
     * 注册服务
     * @param discoveryProperties
     * @param serviceName
     * @param serviceHostAddress
     * @param serviceHostPort
     * @param serverId
     * @param tags
     * @param consulClient
     */
    private void registerAgentService(ThriftServerDiscoveryProperties discoveryProperties, String serviceName,
                                      String serviceHostAddress, Integer serviceHostPort, String serverId,
                                      List<String> tags, ConsulClient consulClient) throws Exception {

        NewService service = new NewService();
        service.setId(serverId);
        service.setAddress(serviceHostAddress);
        service.setPort(serviceHostPort);
        service.setTags(tags);
        service.setName(serviceName);
        registerHealthCheck(discoveryProperties,serviceHostAddress,serviceHostPort,service);
        consulClient.agentServiceRegister(service);
//        registerHealthCheck(discoveryProperties,serviceHostAddress,serviceHostPort,serviceInstance);
    }

    private void registerHealthCheck(ThriftServerDiscoveryProperties discoveryProperties, String serviceHostAddress,
                                     int serviceHostPort, NewService service) {
        ThriftServerHealthCheckProperties healthCheckProperties = discoveryProperties.getHealthCheck();
        Boolean enabled = healthCheckProperties.getEnabled();
        String healthCheckUrl = String.format(HEALTH_CHECK_URL_TEMPLATE,serviceHostAddress,serviceHostPort);
        if (enabled) {
            Long checkInterval = healthCheckProperties.getCheckInterval();
            Long checkTimeout = healthCheckProperties.getCheckTimeout();
            LOGGER.info("Service health check tcp url {}", healthCheckUrl);
            LOGGER.info("Service health check interval {}s, timeout {}s", checkInterval, checkTimeout);
            NewService.Check check = new NewService.Check();
            // TCP:
            check.setTcp(healthCheckUrl);
            check.setInterval(checkInterval.toString()+"s");
            check.setTimeout(checkTimeout.toString()+"s");

            // HTTP: 需要在TServer上实现一个健康检查接口/health
            // 里面可以自定义健康检查的逻辑。相对高端，但是怎么实现啊？
//            check.setHttp(String.format(REGISTRY_URL_TEMPLATE,serviceHostAddress,serviceHostPort)+"/health");
//            check.setInterval(checkInterval.toString()+"s");
//            check.setTimeout(checkTimeout.toString()+"s");
            service.setCheck(check);
        }

    }
}

/**
 *          // Create an instance serializer that will convert between your payload object and bytes
 *         JsonInstanceSerializer<ServicePayload> serializer = new JsonInstanceSerializer<>(ServicePayload.class);
 *
 *         // Build the ServiceInstance
 *         ServiceInstance<ServicePayload> serviceInstance = ServiceInstance.<ServicePayload>builder()
 *                 .name(serviceName)
 *                 .address(address)
 *                 .port(port)
 *                 .payload(payload) // Include your payload object with metadata here
 *                 .build();
 *
 *         // Create the ServiceDiscovery instance
 *         ServiceDiscovery<ServicePayload> serviceDiscovery = ServiceDiscoveryBuilder.builder(ServicePayload.class)
 *                 .client(client)
 *                 .basePath("services")
 *                 // Assign the instance serializer to the service discovery
 *                 .serializer(serializer)
 *                 .build();
 */
