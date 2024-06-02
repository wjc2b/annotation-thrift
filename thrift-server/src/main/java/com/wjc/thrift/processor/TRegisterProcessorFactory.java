package com.wjc.thrift.processor;

import com.wjc.thrift.exception.ThriftServerException;
import com.wjc.thrift.wrapper.ThriftServiceWrapper;
import org.apache.thrift.TProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


/**
 * @author wjc
 * @date 2024-04-06 22:32
 * @desription 统一创建Server
 */
public class TRegisterProcessorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(TRegisterProcessorFactory.class);

    private static class TRegisterProcessorHolder{
        private static final TRegisterProcessor REGISTER_PROCESSOR = new TRegisterProcessor();
    }
    static TRegisterProcessor getRegisterProcessor() {return TRegisterProcessorHolder.REGISTER_PROCESSOR;}

    public static  TRegisterProcessor registerProcessor(List<ThriftServiceWrapper> serviceWrappers) throws NoSuchMethodException {
        if (CollectionUtils.isEmpty(serviceWrappers)){
            throw new ThriftServerException("No Thrift Service wrapper found!");
        }
        TRegisterProcessor registerProcessor = getRegisterProcessor();
        registerProcessor.setProcessorMetaMap(new HashMap<>(serviceWrappers.size()));
        register(serviceWrappers,registerProcessor);
        LOGGER.info("Multiplexed processor totally owns {} service processors", registerProcessor.processorMetaMap.size());

        return registerProcessor;
    }

    private static void register(List<ThriftServiceWrapper> serviceWrappers, TRegisterProcessor registerProcessor) throws NoSuchMethodException {
        for (ThriftServiceWrapper serviceWrapper: serviceWrappers) {
            Object bean = serviceWrapper.getThriftService();
            Class<?> ifaceType = serviceWrapper.getIfaceType();
            if (Objects.isNull(ifaceType)){  // 如果wrapper中的iface类为null,
                // 那么就去bean的接口中找。
                ifaceType = Stream.of(ClassUtils.getAllInterfaces(bean))
                        .filter(clazz -> clazz.getName().endsWith("$Iface")) // 1、第一步找以$Iface结尾的类
                        /**
                         * This method returns null if this class or interface is not a member of any other class. If this Class object represents an array class, a primitive type, or void,then this method returns null.
                         */
                        .filter(iFace -> iFace.getDeclaringClass() != null) // 2、第二步，找内部类或者嵌套类。
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No Thrift IFace found on implementation"));
            }
            Class<TProcessor> processorClass = Stream.of(ifaceType.getDeclaringClass().getDeclaredClasses()) // 第一部分会获取 ifaceType 的声明类，也就是包含 ifaceType 的外部类
                    // 第二部分是获得该类中声明的所有类的Class对象数值。
                    .filter(clazz -> clazz.getName().endsWith("$Processor"))
                    .filter(TProcessor.class::isAssignableFrom)// 判断是不是TProcessor的类或者子类
                    .findFirst()
                    .map(processor -> (Class<TProcessor>) processor) // 强制转化为 (Class<TProcessor>) 类
                    .orElseThrow(() -> new IllegalStateException("No thrift IFace found on implementation"));
            // 从上一步中获得的处理器类的构造器
            Constructor<TProcessor> processorConstructor = processorClass.getConstructor(ifaceType);
            // 根据构造器进行实例化
            TProcessor tProcessor = BeanUtils.instantiateClass(processorConstructor, bean);
            // 一方面把这些数据加入registerProcessor
            String serviceSignature = serviceWrapper.getThriftServiceSignature();
            registerProcessor.processorMetaMap.putIfAbsent(serviceSignature,serviceWrapper);
            LOGGER.info("Processor bean {} with signature [{}] is instantiated",tProcessor,serviceSignature);
            // 另一方面加入 TMultiplexedProcessor 的Map中
            registerProcessor.registerProcessor(serviceSignature,tProcessor);
            LOGGER.info("Single processor {} register onto multiplexed processor with signature [{}]", tProcessor, serviceSignature);
        }
    }


}
