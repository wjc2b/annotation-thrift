package com.wjc.thrift.client.loadbalance;

import com.google.common.collect.Lists;
import com.wjc.thrift.client.common.ThriftServerNode;
import com.wjc.thrift.client.discovery.ServerListUpdater;
import com.wjc.thrift.client.discovery.ThriftConsulServerListUpdater;
import com.wjc.thrift.client.discovery.ThriftConsulServerNode;
import com.wjc.thrift.client.discovery.ThriftConsulServerNodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author wjc
 * @date 2024-04-17 19:32
 * @desription 主要用于从ServerList中根据Loadbalancer选择一个ThriftConsulServerNode出来。
 * 同时提供，从Consul中定时读取存活节点。
 */
public class ThriftConsulServerListLoadBalancer extends AbstractLoadBalancer{
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftConsulServerListLoadBalancer.class);
    private ThriftConsulServerNodeList serverNodeList;
    private IRule rule;

    private volatile ServerListUpdater serverListUpdater;

    // 函数式接口 && 方法引用
    /**
     * 方法引用提供了一种引用直接调用的方法的简洁写法。
     * 如果一个lambda表达式实现了一个函数式接口，并且其主体调用了一个已经存在的方法，那么可以用方法引用来代替。
     * 相当于说：updateListOfServers()方法的签名和返回值，和doUpdate()的方法和返回值一样。
     * 就可以用lambda和方法引用的方式将doUpdate()的实现，用updateListOfServers()代替。
     */
    private final ServerListUpdater.UpdateAction updateAction = this::updateListOfServers;

    public ThriftConsulServerListLoadBalancer(ThriftConsulServerNodeList serverNodeList, IRule rule) {
        this.serverNodeList = serverNodeList;
        this.rule = rule;
        this.serverListUpdater = new ThriftConsulServerListUpdater();
        this.startUpdateAction();
    }

    private synchronized void startUpdateAction() {
        LOGGER.info("Using serverListUpdater {}", serverListUpdater.getClass().getSimpleName());
        if (serverListUpdater == null){
            serverListUpdater = new ThriftConsulServerListUpdater();
        }
        this.serverListUpdater.start(updateAction);
    }

    @Override
    public ThriftConsulServerNodeList getThriftServerNodeList() {
        return this.serverNodeList;
    }

    /**
     * 根据key和loadbalance算法，从Consul中选择一个ServerNode。
     * @param key
     * @return
     */
    @Override
    public ThriftConsulServerNode chooseServerNode(String key) {
        if (rule == null){
            return null;
        }else {
            ThriftServerNode serverNode;
            try {
                serverNode = rule.choose(key);
            } catch (Exception e) {
                LOGGER.warn("LoadBalancer [{}]:  Error choosing server for key {}", getClass().getSimpleName(), key, e);
                return null;
            }
            if (serverNode instanceof ThriftConsulServerNode){
                return (ThriftConsulServerNode) serverNode;
            }
        }
        return null;
    }

    private void updateListOfServers() {
        Map<String, LinkedHashSet<ThriftConsulServerNode>> thriftServers = this.serverNodeList.refreshThriftServers();
        List<String> serverList = Lists.newArrayList();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, LinkedHashSet<ThriftConsulServerNode>> entry:thriftServers.entrySet()){
            serverList.add(
                    sb.append(entry.getKey())
                            .append(":")
                            .append(entry.getValue()).toString());
            // 重置sb
            sb.setLength(0);
        }
        LOGGER.info("Refreshed thrift serverList: [" + String.join(", ", serverList) + "]");

    }

    public void stopServerListRefreshing() {
        if (serverListUpdater != null) {
            serverListUpdater.stop();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ThriftConsulServerListLoadBalancer:");
        sb.append(super.toString());
        sb.append("ServerList:").append(String.valueOf(serverNodeList));
        return sb.toString();
    }
}
