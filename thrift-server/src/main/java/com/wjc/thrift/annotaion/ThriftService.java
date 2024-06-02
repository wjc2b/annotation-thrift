package com.wjc.thrift.annotaion;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author wjc
 * @date 2024-04-06 17:53
 * @desription
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
@Inherited
public @interface ThriftService{

    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    double version() default 1.0;


}
