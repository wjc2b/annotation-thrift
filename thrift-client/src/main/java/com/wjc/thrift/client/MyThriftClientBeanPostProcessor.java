package com.wjc.thrift.client;

import com.wjc.thrift.client.annotation.MyThriftClient;
import com.wjc.thrift.client.annotation.ThriftRefer;
import com.wjc.thrift.client.common.ThriftClientAware;
import com.wjc.thrift.client.exception.ThriftClientInstantiateException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * @author wjc
 * @date 2024-04-15 21:31
 * @desription
 */
public class MyThriftClientBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyThriftClientBeanPostProcessor.class);
    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Object target = bean;
        if (AopUtils.isJdkDynamicProxy(target)){
           TargetSource targetSource = ((Advised)target).getTargetSource();
           if (LOGGER.isDebugEnabled()){
               LOGGER.info("Target Obj {} uses jdk proxy",targetSource);
           }
           try {
                target = targetSource.getTarget();
           } catch (Exception e) {
               throw new RuntimeException(e);
           }
        }
        if (AopUtils.isCglibProxy(target)){
            TargetSource targetSource = ((Advised)target).getTargetSource();
            if (LOGGER.isDebugEnabled()){
                LOGGER.info("Target Obj {} uses cglib proxy",targetSource);
            }
            try {
                target = targetSource.getTarget();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Class<?> targetClass = target.getClass();
        final Object targetBean = target;
        // 动态地将实现了特定服务的bean注入到带有ThriftRefer注解的字段中。
        ReflectionUtils.doWithFields(targetClass,field -> {// 它允许我们对给定类中的每个字段（包括继承的字段）执行某些操作，相当于是字段的迭代器。
            // 在所有的字段中，找到用ThriftRefer注释的字段。
            MyThriftClient myThriftClient = AnnotationUtils.findAnnotation(field, MyThriftClient.class);
            String referName = StringUtils.isNoneBlank(myThriftClient.value()) ? myThriftClient.value() : myThriftClient.name();
            Class<?> type = field.getType();
            Object injectedBean;
            if (StringUtils.isNoneBlank(referName)){
                injectedBean = applicationContext.getBean(type,referName);
                Optional.ofNullable(injectedBean)
                        .orElseThrow(() -> new ThriftClientInstantiateException("Detected non-qualified bean with name {}" + referName));
                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field,targetBean,injectedBean);
            }else{
                Map<String, ?> beansOfType = applicationContext.getBeansOfType(field.getType());
                if (MapUtils.isEmpty(beansOfType)){
                    throw new ThriftClientInstantiateException("Detected non-qualified bean of {}" + type.getSimpleName());
                }
                if (beansOfType.size() > 1) {
                    throw new ThriftClientInstantiateException("Detected ambiguous beans of {}" + type.getSimpleName());
                }
                injectedBean = beansOfType.entrySet().stream()
                        .findFirst()
                        .map(Map.Entry::getValue)
                        .orElseThrow(()-> new ThriftClientInstantiateException("Detected non-qualified bean of {}" + type.getSimpleName()));
                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field,targetBean,injectedBean);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Bean {} is injected into target bean {}, field {}", injectedBean, targetBean, field.getName());
            }
        },field -> (AnnotationUtils.getAnnotation(field,MyThriftClient.class)!=null));

        // 配合下一个doWithMethod使用
        ReflectionUtils.MethodFilter methodFilter = method->{
            boolean basicCondition = AnnotationUtils.getAnnotation(method, MyThriftClient.class) !=null
                    && method.getParameterCount() > 0
                    && method.getReturnType() == Void.TYPE;
            if (!basicCondition){
                return false;
            }
            // 如果方法的所有参数都是ThriftClientAware的子类，且方法返回值为void，那么就返回true。
            // 反之返回false/
            return Arrays.stream(method.getParameters())
                    .map(Parameter::getType)
                    .map(ThriftClientAware.class::isAssignableFrom)
                    .reduce((param1,param2) -> param1 && param2)
                    .get();
        };
        ReflectionUtils.doWithMethods(targetClass,method -> {
            Parameter[] parameters = method.getParameters();
            Object objectArray = Arrays.stream(parameters).map(parameter -> {
                Class<?> parameterType = parameter.getType();
                Map<String, ?> injectedBeanMap = applicationContext.getBeansOfType(parameterType);
                if (MapUtils.isEmpty(injectedBeanMap)) {
                    throw new ThriftClientInstantiateException("Detected non-qualified bean of {}" + parameterType.getSimpleName());
                }
                if (injectedBeanMap.size() > 1) {
                    throw new ThriftClientInstantiateException("Detected ambiguous beans of {}" + parameterType.getSimpleName());
                }
                return injectedBeanMap.entrySet().stream()
                        .findFirst()
                        .map(Map.Entry::getValue)
                        .orElseThrow(() -> new ThriftClientInstantiateException(
                                "Detected non-qualified bean of {}" + parameterType.getSimpleName()));
            }).toArray();
            ReflectionUtils.makeAccessible(method);
            ReflectionUtils.invokeMethod(method,targetBean,objectArray);
        },methodFilter);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
