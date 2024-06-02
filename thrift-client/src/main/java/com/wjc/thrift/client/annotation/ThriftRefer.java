package com.wjc.thrift.client.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author wjc
 * @date 2024-04-15 22:03
 * @desription
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD})
@Documented
@Inherited
public @interface ThriftRefer {
    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";
}
