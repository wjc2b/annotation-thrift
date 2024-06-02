package com.wjc.thrift.client.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author wjc
 * @date 2024-06-01 22:47
 * @desription
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface MyThriftClient {

    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    String serviceId();

    double version() default 1.0;
}
