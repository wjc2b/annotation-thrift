package com.wjc.thrift.client.properties;

import com.wjc.thrift.client.common.ThriftClientContext;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * @author wjc
 * @date 2024-04-16 16:18
 * @desription
 */
public class ConsulPropertiesCondition extends SpringBootCondition {
    private static final String SPRING_CLOUD_CURATOR_HOST = "spring.cloud.consul.host";
    private static final String SPRING_CLOUD_CURATOR_PORT = "spring.cloud.consul.port";
    private static final String ADDRESS_TEMPLATE = "%s:%d";
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        String address = environment.getProperty(SPRING_CLOUD_CURATOR_HOST);
        int port = environment.getProperty(SPRING_CLOUD_CURATOR_PORT,int.class);
        String format = String.format(ADDRESS_TEMPLATE, address, port);

        ThriftClientContext.registry(format);

        return new ConditionOutcome(!StringUtils.isEmpty(address)&&port>0
                ,"Consul server address is " + format);
    }
}
