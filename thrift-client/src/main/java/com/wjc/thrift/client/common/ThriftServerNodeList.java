package com.wjc.thrift.client.common;

import com.google.common.collect.Maps;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @author wjc
 * @date 2024-04-17 15:35
 * @desription
 */
public abstract class ThriftServerNodeList<T extends ThriftServerNode> implements ServerNodeList<T> {
    protected Map<String, LinkedHashSet<T>> serverNodeMap =  Maps.newConcurrentMap();

    public Map<String, LinkedHashSet<T>> getServerNodeMap() {
        return serverNodeMap;
    }

    @Override
    public Map<String, LinkedHashSet<T>> getInitialListOfThriftServers() {
        return getThriftServers();
    }

    @Override
    public Map<String, LinkedHashSet<T>> getUpdatedListOfThriftServers() {
        return refreshThriftServers();
    }

    public abstract Map<String, LinkedHashSet<T>> refreshThriftServers();
    public abstract List<T> refreshThriftServers(String serviceName);

    public abstract Map<String, LinkedHashSet<T>> getThriftServers();
    public abstract List<T> getThriftServers(String serviceName);
}
