package com.wjc.thrift.client.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author wjc
 * @date 2024-04-16 10:22
 * @desription 类、接口、枚举类的注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface ThriftClient {
    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    String serviceId();

    double version() default 1.0;

    Class<?> refer();

}
