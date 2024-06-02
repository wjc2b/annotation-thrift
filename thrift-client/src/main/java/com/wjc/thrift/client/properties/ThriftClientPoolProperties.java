package com.wjc.thrift.client.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author wjc
 * @date 2024-04-16 10:08
 * @desription
 */
@Getter
@Setter
@ConfigurationProperties("spring.thrift.client.pool")
public class ThriftClientPoolProperties {
    private int retryTimes = 3;

    private int connectTimeout = 10000;

    private int poolMaxTotalPerKey = 60;

    private int poolMaxIdlePerKey = 40;

    private int poolMinIdlePerKey = 3;

    private long poolMaxWait = 180000;

    /**
     * 池对象创建时时验证是否正常可用
     */
    private boolean testOnCreate = true;

    /**
     * 池对象借出时验证是否正常可用
     */
    private boolean testOnBorrow = true;


    /**
     * 池对象归还时验证是否正常可用
     */
    private boolean testOnReturn = true;

    /**
     * 空闲连接自动被空闲连接回收器
     */
    private boolean isTestWhileIdle = true;
}
