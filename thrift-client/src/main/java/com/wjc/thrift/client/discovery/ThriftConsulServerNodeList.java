package com.wjc.thrift.client.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.health.model.HealthService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.health.HealthCheck;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.option.QueryOptions;
import com.wjc.thrift.client.common.ThriftServerNodeList;
import com.wjc.thrift.client.exception.ThriftClientException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @author wjc
 * @date 2024-04-17 15:30
 * @desription 获取Consul中的ServerNodeList
 * 这里有两套方式：
 * 1、带参数获取，你可以通过serverName来从Consul中获取相关的服务，并更新到this.serverNodeMap中。
 * 2、不带参数获取，你可以从catalogClient中获取所有注册在Consul上的节点，然后获取其中通过健康检查的那部分，存在this.serverNodeMap中
 * 但是这种方式，存的是一个不可变的Map。
 */
public class ThriftConsulServerNodeList extends ThriftServerNodeList<ThriftConsulServerNode> {

    private final Consul consul;
    private final CatalogClient catalogClient;
    private final HealthClient healthClient;

    private static volatile ThriftConsulServerNodeList serverNodeList = null;

    private ThriftConsulServerNodeList(Consul consul) {
        this.consul = consul;
        this.catalogClient = this.consul.catalogClient();
        this.healthClient = this.consul.healthClient();
    }

    public static ThriftConsulServerNodeList singleton(Consul consul){
        if (serverNodeList==null){
            synchronized (ThriftConsulServerNodeList.class){
                if (serverNodeList==null){
                    serverNodeList = new ThriftConsulServerNodeList(consul);
                }
            }
        }
        return serverNodeList;
    }

    /**
     * catalogClient 是查询整个Consul集群中所有服务和节点的理想选择。
     * healthClient包含所有通过健康状态选择的节点。
     *
     * 这个函数就是从所有节点中找出那些健康节点。
     * @return
     */
    @Override
    public Map<String, LinkedHashSet<ThriftConsulServerNode>> refreshThriftServers() {
        // 返回Consul中的所有服务。QueryOptions.BLANK：默认查询行为。
        Map<String, List<String>> catalogServiceMap = this.catalogClient.getServices(QueryOptions.BLANK).getResponse();
        if (MapUtils.isEmpty(catalogServiceMap)){
            return this.serverNodeMap;
        }

        Map<String,LinkedHashSet<ThriftConsulServerNode>> serverNodeMap = Maps.newConcurrentMap();
        for(Map.Entry<String, List<String>> catalogServiceEntry: catalogServiceMap.entrySet()){
            String serviceName = catalogServiceEntry.getKey();
            List<String> tags = catalogServiceEntry.getValue();

            if (CollectionUtils.isEmpty(tags)) {
                continue;
            }
            List<ServiceHealth> serviceHealthList = this.healthClient.getAllServiceInstances(serviceName).getResponse();
            LinkedHashSet<ThriftConsulServerNode> serverNodeSet = Sets.newLinkedHashSet();
            List<ThriftConsulServerNode> serverNodeList = Lists.newArrayList(serverNodeSet);
            // 通过遍历现有健康节点，来获得注册的ServerNode
            filterAndCompoServerNodes(serverNodeList,serviceHealthList);
            // 这里有问题啊，遍历完不为空，为什么转而去判断serverNodeSet？
            // TODO：逻辑问题
            serverNodeSet.addAll(serverNodeList);
            if (CollectionUtils.isNotEmpty(serverNodeSet)) {
                serverNodeMap.put(serviceName, serverNodeSet);
            }
        }
        this.serverNodeMap.clear();
        this.serverNodeMap.putAll(serverNodeMap);
        // 返回一个不可变的Map。
        return ImmutableMap.copyOf(this.serverNodeMap);
    }

    @Override
    public List<ThriftConsulServerNode> refreshThriftServers(String serviceName) {
        List<ThriftConsulServerNode> serverNodeList = Lists.newArrayList();
        List<ServiceHealth> serviceHealthList = this.healthClient.getAllServiceInstances(serviceName).getResponse();
        filterAndCompoServerNodes(serverNodeList,serviceHealthList);
        if (CollectionUtils.isNotEmpty(serverNodeList)){
            this.serverNodeMap.put(serviceName,new LinkedHashSet<>(serverNodeList));
        }
        return serverNodeList;
    }

    /**
     * 对于健康的节点，检查是否符合要求，把符合要求的放入serverNodeList中。
     * @param serverNodeList
     * @param serviceHealthList
     */
    private void filterAndCompoServerNodes(List<ThriftConsulServerNode> serverNodeList,
                                           List<ServiceHealth> serviceHealthList) {
        for(ServiceHealth serviceHealth:serviceHealthList){
            ThriftConsulServerNode serverNode = getThriftConsulServerNode(serviceHealth);
            if (serverNode == null){
                continue;
            }
            if (!serverNode.isHealth()){
                continue;
            }
            if (CollectionUtils.isEmpty(serverNode.getTags())){
                continue;
            }
            serverNodeList.add(serverNode);
        }

    }

    private static ThriftConsulServerNode getThriftConsulServerNode(ServiceHealth serviceHealth) {
        ThriftConsulServerNode serverNode = new ThriftConsulServerNode();

        Node node = serviceHealth.getNode();
        serverNode.setNode(node.getNode());

        Service service = serviceHealth.getService();
        serverNode.setAddress(service.getAddress());
        serverNode.setPort(service.getPort());
        serverNode.setHost(ThriftConsulServerUtils.findHost(serviceHealth));

        serverNode.setServiceId(service.getService());
        serverNode.setTags(service.getTags());
        // 是否通过健康检查
        serverNode.setHealth(ThriftConsulServerUtils.isPassingCheck(serviceHealth));
        return serverNode;
    }

    @Override
    public Map<String, LinkedHashSet<ThriftConsulServerNode>> getThriftServers() {
        if (MapUtils.isNotEmpty(this.serverNodeMap)){
            return this.serverNodeMap;
        }
        return refreshThriftServers();
    }

    @Override
    public List<ThriftConsulServerNode> getThriftServers(String serviceName) {
        if (MapUtils.isNotEmpty(this.serverNodeMap) && (this.serverNodeMap.containsKey(serviceName))){
            LinkedHashSet<ThriftConsulServerNode> serverNodes = this.serverNodeMap.get(serviceName);
            if (CollectionUtils.isNotEmpty(serverNodes)){
                return Lists.newArrayList(serverNodes);
            }
        }
        return refreshThriftServers(serviceName);
    }
    private static class ThriftConsulResponseCallback implements ConsulResponseCallback<List<ServiceHealth>> {

        List<ThriftConsulServerNode> serverNodeList;

        public ThriftConsulResponseCallback(List<ThriftConsulServerNode> serverNodeList) {
            this.serverNodeList = serverNodeList;
        }

        @Override
        public void onComplete(ConsulResponse<List<ServiceHealth>> consulResponse) {
            List<ServiceHealth> response = consulResponse.getResponse();
            for (ServiceHealth healthService : response) {
                ThriftConsulServerNode serverNode = getThriftConsulServerNode(healthService);
                serverNodeList.add(serverNode);
            }
        }

        @Override
        public void onFailure(Throwable throwable) {
            throw new ThriftClientException("Failed to query service instances from consul agent", throwable);
        }

    }
}
