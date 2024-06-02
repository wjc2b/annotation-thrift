package com.wjc.thrift.annotaion;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author wjc
 * @date 2024-04-13 22:24
 * @desription
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(ThriftServerConfigurationSelector.class)
public @interface EnableThriftServer {
}
