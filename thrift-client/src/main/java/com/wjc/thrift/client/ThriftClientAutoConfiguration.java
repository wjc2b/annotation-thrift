package com.wjc.thrift.client;

import com.wjc.thrift.client.common.ThriftClientContext;
import com.wjc.thrift.client.pool.TransportKeyedObjectPool;
import com.wjc.thrift.client.pool.TransportKeyedPooledObjectFactory;
import com.wjc.thrift.client.properties.ConsulPropertiesCondition;
import com.wjc.thrift.client.properties.ThriftClientPoolProperties;
import com.wjc.thrift.client.properties.ThriftClientProperties;
import com.wjc.thrift.client.properties.ThriftClientPropertiesCondition;
import com.wjc.thrift.client.scanner.MyThriftClientRegistrar;
import com.wjc.thrift.client.scanner.ThriftClientBeanScanProcessor;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @author wjc
 * @date 2024-04-16 10:12
 * @desription
 */
@Configuration
@Conditional(value = {ConsulPropertiesCondition.class, ThriftClientPropertiesCondition.class})
@EnableConfigurationProperties(ThriftClientProperties.class)
public class ThriftClientAutoConfiguration {

    /**
     * 定义包扫描bean，注入的时候，会把指定包下面的bean全部装配，
     * TODO: 但这个和直接使用注解有什么区别？
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public ThriftClientBeanScanProcessor thriftClientBeanScanProcessor(){
        return new ThriftClientBeanScanProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public MyThriftClientBeanPostProcessor myThriftClientBeanPostProcessor(){
        return new MyThriftClientBeanPostProcessor();
    }

    /**
     *
     * @param properties
     * @return
     */
    @Bean
    public GenericKeyedObjectPoolConfig keyedObjectPoolConfig(ThriftClientProperties properties){
        ThriftClientPoolProperties poolProperties = properties.getPool();
        GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
        config.setMinIdlePerKey(poolProperties.getPoolMinIdlePerKey());
        config.setMaxIdlePerKey(poolProperties.getPoolMaxIdlePerKey());
        config.setMaxWaitMillis(poolProperties.getPoolMaxWait());
        config.setMaxTotalPerKey(poolProperties.getPoolMaxTotalPerKey());
        config.setTestOnCreate(poolProperties.isTestOnCreate());
        config.setTestOnBorrow(poolProperties.isTestOnBorrow());
        config.setTestOnReturn(poolProperties.isTestOnReturn());
        config.setTestWhileIdle(poolProperties.isTestWhileIdle());
        config.setFairness(true);
        config.setJmxEnabled(false);
        System.out.println("GenericKeyedObjectPoolConfig finished" );
        return config;
    }

    @Bean
//    @ConditionalOnMissingBean
    public TransportKeyedPooledObjectFactory transportKeyedPooledObjectFactory(ThriftClientProperties properties){
        System.out.println("TransportKeyedPooledObjectFactory finished" );
        return new TransportKeyedPooledObjectFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public TransportKeyedObjectPool transportKeyedObjectPool(GenericKeyedObjectPoolConfig config,
                                                             TransportKeyedPooledObjectFactory poolFactory){
        System.out.println("TransportKeyedObjectPool finished" );
        return new TransportKeyedObjectPool(poolFactory,config);
    }

    /*
    注入 ThriftClientBeanPostProcessor

     */
    @Bean
    @ConditionalOnMissingBean
    public ThriftClientBeanPostProcessor thriftClientBeanPostProcessor(){
        ThriftClientBeanPostProcessor thriftClientBeanPostProcessor = new ThriftClientBeanPostProcessor();
        System.out.println("ThriftClientBeanPostProcessor finished" );
        return thriftClientBeanPostProcessor;
    }

    @Bean
    @ConditionalOnMissingBean
    public ThriftClientContext thriftClientContext(
            ThriftClientProperties properties, TransportKeyedObjectPool objectPool) {
        System.out.println("ThriftClientContext finished" );
        return ThriftClientContext.context(properties, objectPool);
    }


}
