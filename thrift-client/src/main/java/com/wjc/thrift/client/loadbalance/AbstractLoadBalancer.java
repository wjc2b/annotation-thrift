package com.wjc.thrift.client.loadbalance;

import com.wjc.thrift.client.discovery.ThriftConsulServerNode;
import com.wjc.thrift.client.discovery.ThriftConsulServerNodeList;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @author wjc
 * @date 2024-04-17 19:29
 * @desription
 */
public abstract class AbstractLoadBalancer implements ILoadBalancer<ThriftConsulServerNode> {

    public abstract ThriftConsulServerNodeList getThriftServerNodeList();
    public abstract ThriftConsulServerNode chooseServerNode(String key);

    @Override
    public Map<String, LinkedHashSet<ThriftConsulServerNode>> getAllServerNodes() {
        return getThriftServerNodeList().getServerNodeMap();
    }

    @Override
    public Map<String, LinkedHashSet<ThriftConsulServerNode>> getRefreshedServerNodes() {
        return getThriftServerNodeList().refreshThriftServers();
    }

    @Override
    public List<ThriftConsulServerNode> getServerNodes(String key) {
        return getThriftServerNodeList().getThriftServers(key);
    }

    @Override
    public List<ThriftConsulServerNode> getRefreshedServerNodes(String key) {
        return getThriftServerNodeList().refreshThriftServers(key);
    }
}
