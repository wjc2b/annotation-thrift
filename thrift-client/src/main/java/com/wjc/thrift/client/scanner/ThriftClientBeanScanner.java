package com.wjc.thrift.client.scanner;

import com.wjc.thrift.client.annotation.MyThriftClient;
import com.wjc.thrift.client.annotation.ThriftClient;
import com.wjc.thrift.client.common.ThriftClientDefinitionProperty;
import com.wjc.thrift.client.common.ThriftServiceSignature;
import com.wjc.thrift.client.exception.ThriftClientConfigException;
import com.wjc.thrift.client.exception.ThriftClientInstantiateException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TProcessor;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;

/**
 * @author wjc
 * @date 2024-04-16 10:21
 * @desription
 */
public class ThriftClientBeanScanner extends ClassPathBeanDefinitionScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftClientBeanScanner.class);
    public ThriftClientBeanScanner(BeanDefinitionRegistry definitionRegistry) {
        super(definitionRegistry);
    }

    @Override
    protected void registerDefaultFilters() {
//        this.addIncludeFilter(new AnnotationTypeFilter(MyThriftClient.class));
        this.addIncludeFilter(new AnnotationTypeFilter(ThriftClient.class));
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> definitionHolders = super.doScan(basePackages);
        LOGGER.info("Packages scanned by thriftClientBeanDefinitionScanner is [{}]",
                StringUtils.join(basePackages, ", "));
        for(BeanDefinitionHolder definitionHolder: definitionHolders){
            GenericBeanDefinition definition = (GenericBeanDefinition) definitionHolder.getBeanDefinition();
            LOGGER.info("Scanned and found thrift client, bean {} assigned from {}",
                    definitionHolder.getBeanName(),
                    definition.getBeanClassName());
            Class<?> beanClass;
            try {
                beanClass = Class.forName(definition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            }
            ThriftClient thriftClient = AnnotationUtils.findAnnotation(beanClass, ThriftClient.class);
            if (thriftClient == null) {
                LOGGER.warn("Thrift client is not found");
                continue;
            }
            String beanName = StringUtils.isNoneBlank(thriftClient.value())
                    ? thriftClient.value()
                    :(StringUtils.isNotBlank(thriftClient.name()) ? thriftClient.name() : StringUtils.uncapitalize(beanClass.getSimpleName()));
            definition.getPropertyValues().addPropertyValue(ThriftClientDefinitionProperty.BEAN_NAME,beanName);
            definition.getPropertyValues().addPropertyValue(ThriftClientDefinitionProperty.BEAN_CLASS,beanClass);
            definition.getPropertyValues().addPropertyValue(ThriftClientDefinitionProperty.BEAN_CLASS_NAME,beanClass.getName());

            Class<?> refer = thriftClient.refer();
            definition.getPropertyValues().addPropertyValue(ThriftClientDefinitionProperty.SERVICE_CLASS,refer);
            ThriftServiceSignature serviceSignature = new ThriftServiceSignature(thriftClient.serviceId(), thriftClient.refer(), thriftClient.version());
            Class<? extends TServiceClient> clientClass = getClientClassFromAnnotation(beanClass);
            Constructor<? extends TServiceClient> constructor;

            try {
                constructor = clientClass.getConstructor(TProtocol.class);
            } catch (NoSuchMethodException e) {
                LOGGER.error(e.getMessage(), e);
                throw new ThriftClientInstantiateException("Failed to get constructor with args TProtocol", e);
            }
            definition.getPropertyValues().addPropertyValue(ThriftClientDefinitionProperty.SERVICE_SIGNATURE,serviceSignature);
            definition.getPropertyValues().addPropertyValue(ThriftClientDefinitionProperty.CLIENT_CLASS,clientClass);
            definition.getPropertyValues().addPropertyValue(ThriftClientDefinitionProperty.CLIENT_CONSTRUCTOR,constructor);
            // 最后生成一个 ThriftClientFactoryBean 类
            definition.setBeanClass(ThriftClientFactoryBean.class);
        }
        return definitionHolders;
    }

    /**
     * 从 标记了 @ThriftClient 注解的类中获取 客户端类(TServiceClient的子类)。
     * public interface CalculatorThriftClient extends ThriftClientAware<CalculatorService.Client> {}
     *
     * @param beanClass
     * @return
     */
    private Class<? extends TServiceClient> getClientClassFromAnnotation(Class<?> beanClass) {
        // 获取该类实现的接口，上面这例子就是：ThriftClientAware<CalculatorService.Client>
        ParameterizedType clientAwareType = (ParameterizedType) beanClass.getGenericInterfaces()[0];
        if (Objects.isNull(clientAwareType)) {
            throw new ThriftClientConfigException("Interface annotated with @ThriftClient should be inherited from ThriftClientAware");
        }
        // 获得泛型接口的实际参数，上面的例子就是：CalculatorService.Client.class
        Type[] typeArguments = clientAwareType.getActualTypeArguments();
        if (ArrayUtils.isEmpty(typeArguments) || typeArguments.length == 0) {
            throw new ThriftClientConfigException("ThriftClientAware should declare an argument");
        }
        // 上面的例子就是：CalculatorService.Client.class
        Class<?> typeArgument = (Class<?>) typeArguments[0];
        // 验证泛型类似是不是TServiceClient.class 的子类。
        if (!ClassUtils.isAssignable(TServiceClient.class, typeArgument)) {
            throw new ThriftClientConfigException("ThriftClientAware without argument inherited from TServiceClient");
        }
        return (Class<? extends TServiceClient>) typeArgument;
    }
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.hasAnnotation(ThriftClient.class.getName())
                && metadata.isInterface();
    }
}
