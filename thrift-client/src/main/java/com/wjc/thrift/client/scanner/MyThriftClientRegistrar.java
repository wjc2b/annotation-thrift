package com.wjc.thrift.client.scanner;

import com.wjc.thrift.client.annotation.MyThriftClient;
import com.wjc.thrift.client.common.ThriftClientDefinitionProperty;
import com.wjc.thrift.client.common.ThriftServiceSignature;
import com.wjc.thrift.client.exception.ThriftClientInstantiateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author wjc
 * @date 2024-06-02 9:32
 * @desription
 */
@Component
public class MyThriftClientRegistrar implements BeanDefinitionRegistryPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyThriftClientRegistrar.class);

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        for (String beanDefinitionName:registry.getBeanDefinitionNames()){
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanDefinitionName);
            String beanClassName = beanDefinition.getBeanClassName();
            try {
                Class<?> beanClass = Class.forName(beanClassName);
                for (Field field:beanClass.getDeclaredFields()){
                    if (field.isAnnotationPresent(MyThriftClient.class)){
                        GenericBeanDefinition definition = new GenericBeanDefinition();
                        MyThriftClient myThriftClient = AnnotationUtils.findAnnotation(field, MyThriftClient.class);
                        if (myThriftClient == null) {
                            LOGGER.warn("Thrift client is not found");
                            continue;
                        }
                        String beanName = StringUtils.isNoneBlank(myThriftClient.value())
                                ? myThriftClient.value()
                                :(StringUtils.isNotBlank(myThriftClient.name()) ? myThriftClient.name() : StringUtils.uncapitalize(beanClass.getSimpleName()));
                        definition.getPropertyValues().addPropertyValue(ThriftClientDefinitionProperty.BEAN_NAME,beanName);
                        definition.getPropertyValues().addPropertyValue(ThriftClientDefinitionProperty.BEAN_CLASS,field.getType());
                        definition.getPropertyValues().addPropertyValue(ThriftClientDefinitionProperty.BEAN_CLASS_NAME,field.getType().getName());

                        Class<?> declaringClass = field.getType().getDeclaringClass(); // 获得xxxService.class
                        Class<? extends TServiceClient> clientClass = getClientClassFromSuperClass(declaringClass);
                        String serviceId = myThriftClient.serviceId();
                        double version = myThriftClient.version();
                        ThriftServiceSignature serviceSignature = new ThriftServiceSignature(serviceId, declaringClass, version);
                        Constructor<? extends TServiceClient> constructor;
                        try {
                            constructor = clientClass.getConstructor(TProtocol.class);
                        } catch (NoSuchMethodException e) {
                            LOGGER.error(e.getMessage(), e);
                            throw new ThriftClientInstantiateException("Failed to get constructor with args TProtocol", e);
                        }
                        definition.getPropertyValues().addPropertyValue(ThriftClientDefinitionProperty.CLIENT_CLASS,clientClass);
                        definition.getPropertyValues().addPropertyValue(ThriftClientDefinitionProperty.CLIENT_CONSTRUCTOR,constructor);
                        definition.getPropertyValues().addPropertyValue(ThriftClientDefinitionProperty.SERVICE_SIGNATURE,serviceSignature);

                        definition.setBeanClass(ThriftClientFactoryBean.class);
                        definition.setScope("singleton");
                        // 注册成Bean
                        registry.registerBeanDefinition(beanName,definition);
                    }
                }
            }catch (Exception e){

            }
        }
    }

    private Class<? extends TServiceClient> getClientClassFromSuperClass(Class<?> declaringClass) {
        Class<?> client = Arrays.stream(declaringClass.getDeclaredClasses())
                .filter(clazz -> TServiceClient.class.isAssignableFrom(clazz))
                .findFirst()
                .orElseThrow(() -> new ThriftClientInstantiateException("No Client Find in Service Class!"));

        return (Class<? extends TServiceClient>) client;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
