package com.wjc.thrift;


import com.wjc.thrift.annotaion.ThriftService;
import com.wjc.thrift.context.AbstractThriftServerContext;
import com.wjc.thrift.context.ThriftServerContext;
import com.wjc.thrift.exception.ThriftServerException;
import com.wjc.thrift.properties.ThriftServerProperties;
import com.wjc.thrift.wrapper.ThriftServiceWrapper;
import com.wjc.thrift.wrapper.ThriftServiceWrapperFactory;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wjc
 * @date 2024-04-05 20:35
 * @desription
 */
@Configuration
@ConditionalOnProperty(value = "spring.thrift.server.server-id", matchIfMissing = false)
@EnableConfigurationProperties(ThriftServerProperties.class)
public class ThriftServerAutoConfiguration implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftServerAutoConfiguration.class);
    private ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    public ThriftServiceGroup thriftServerGroup(ThriftServerProperties properties) throws TTransportException, IOException {
        // 通过 ApplicationContext 获得所有带有 ThriftService 注解的Bean.
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(ThriftService.class);
        if (beanNames.length == 0){
            LOGGER.error("Can't search any thrift service annotated with @ThriftService");
            throw new ThriftServerException("Can not find any thrift service");
        }
        List<ThriftServiceWrapper> serviceWrappers = Arrays.stream(beanNames).distinct().map(beanName ->{
            Object bean = applicationContext.getBean(beanName);
            Object target = bean;

            ThriftService thriftService = bean.getClass().getAnnotation(ThriftService.class);
            String thriftServiceName = StringUtils.hasLength(thriftService.value())? thriftService.value():beanName;
            if (target instanceof Advised){
                // 如果这是一个代理类的话，就要找到原始的类。
                final Object targetBean = target;
                TargetSource targetSource = ((Advised) target).getTargetSource();
                try {
                    target = targetSource.getTarget();  // ?没有用到啊?
                } catch (Exception e) {
                    throw new ThriftServerException("Failed to get Target from"+target,e);
                }
                return ThriftServiceWrapperFactory.wrapper(properties.getServerId(), thriftServiceName,target,thriftService.version());
            }
            return ThriftServiceWrapperFactory.wrapper(properties.getServerId(), thriftServiceName,target,thriftService.version());
        }).collect(Collectors.toList());
        AbstractThriftServerContext serverContext = new ThriftServerContext(properties,serviceWrappers);
        return new ThriftServiceGroup(serverContext.buildServer());
    }

    @Bean
    public ThriftServerBootstrap thriftServerBootstrap(ThriftServiceGroup thriftServiceGroup){
        return new ThriftServerBootstrap(thriftServiceGroup);
    }


}
