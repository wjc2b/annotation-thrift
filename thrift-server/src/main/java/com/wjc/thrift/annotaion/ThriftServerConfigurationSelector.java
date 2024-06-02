package com.wjc.thrift.annotaion;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.commons.util.SpringFactoryImportSelector;

import java.util.Set;

/**
 * @author wjc
 * @date 2024-04-13 22:26
 * @desription
 */
public class ThriftServerConfigurationSelector extends SpringFactoryImportSelector<EnableThriftServer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftServerConfigurationSelector.class);

    private static final Set<String> SERVICE_MODEL_SET = Sets.newHashSet("simple",
            "nonBlocking", "threadPool", "hsHa", "threadedSelector");
    private static final String SERVER_ID = "spring.thrift.server.server-id";
    private static final String SERVICE_MODEL = "spring.thrift.server.service-model";
    private static final String PORT = "spring.thrift.server.port";
    @Override
    protected boolean isEnabled() {
        String serviceId = getEnvironment().getProperty(SERVER_ID, String.class);
        String serviceModel = getEnvironment().getProperty(SERVICE_MODEL, String.class);
        Integer port = getEnvironment().getProperty(PORT, Integer.class);
        boolean enableAutoConfiguration = StringUtils.isNotBlank(serviceId) && SERVICE_MODEL_SET.contains(serviceModel) && port > 0;
        if (enableAutoConfiguration) {
            LOGGER.info("Enable thrift server auto configuration, service id {}, service model {}, port {}",
                    serviceId, serviceModel, port);
        }
        return enableAutoConfiguration;
    }
}
