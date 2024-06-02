package com.wjc.thrift.client.common;

import java.util.LinkedHashSet;
import java.util.Map;

/**
 * @author wjc
 * @date 2024-04-17 15:35
 * @desription 用于获取和更新节点
 */
public interface ServerNodeList<T> {
    Map<String, LinkedHashSet<T>> getInitialListOfThriftServers();

    Map<String, LinkedHashSet<T>> getUpdatedListOfThriftServers();
}
