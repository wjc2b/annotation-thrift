package com.wjc.thrift.client.loadbalance;

import com.google.common.collect.Lists;
import com.wjc.thrift.client.common.ThriftServerNode;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wjc
 * @date 2024-04-17 16:53
 * @desription
 */
public class RoundRobinRule extends AbstractLoadBalancerRule{
    private static final Logger LOGGER = LoggerFactory.getLogger(RoundRobinRule.class);

    private AtomicInteger nextServerCyclicCounter;

    public RoundRobinRule() {
        this.nextServerCyclicCounter = new AtomicInteger();
    }
    public RoundRobinRule(ILoadBalancer lb){
        this();
        setLoadBalancer(lb);
    }

    @Override
    public ThriftServerNode choose(String key) {
        return choose(getLoadBalancer(),key);
    }
    private ThriftServerNode choose(ILoadBalancer lb,String key){
        if (lb==null){
            LOGGER.warn("No specified load balancer");
            return null;
        }
        List<ThriftServerNode> serverNodes;
        ThriftServerNode serverNode = null;
        int count = 0;
        while(serverNode==null && count++<10){
            serverNodes = lb.getServerNodes(key);
            if (CollectionUtils.isEmpty(serverNodes)){
                Map<String, LinkedHashSet<ThriftServerNode>> serverNodesMap = lb.getAllServerNodes();
                if (MapUtils.isEmpty(serverNodesMap) || !serverNodesMap.containsKey(key)) {
                    LOGGER.warn("No up servers of key {}, available from load balancer: " + lb, key);
                    return null;
                }
                LinkedHashSet<ThriftServerNode> thriftServerNodes = serverNodesMap.get(key);
                if (org.apache.commons.collections4.CollectionUtils.isEmpty(thriftServerNodes)) {
                    LOGGER.warn("No up servers of key {}, available from load balancer: " + lb, key);
                    return null;
                }
                serverNodes = Lists.newArrayList(thriftServerNodes);
            }
            int nextServerIndex = incrementAndGetModulo(serverNodes.size());
            serverNode = serverNodes.get(nextServerIndex);
            if (serverNode == null){
                Thread.yield();
            }
        }
        if (count>=10){
            LOGGER.warn("No available alive server nodes after 10 tries from load balancer: "
                    + lb);
        }
        return serverNode;
    }

    private int incrementAndGetModulo(int size) {
        // CAS自选的操作是合理的。不过，这种方法在高并发环境中可能会遇到大量的自旋等待，
        // 尤其是当 modulo 很小或者请求非常频繁时，因为多个线程可能会竞争更新同一个值。
        // 在这种情况下，可能会有一定的性能损耗，因为线程可能需要多次循环才能成功更新计数器。
        /**
         * for (;;){
         *             int current = nextServerCyclicCounter.get();
         *             int next = (current+1)%size;
         *             if (nextServerCyclicCounter.compareAndSet(current,next)){
         *                 return next;
         *             }
         *         }
         */
        // 直接采用getAndIncrement()方法
        // 1、获取当前值，然后+1(这个操作是线程安全的)
        // 2、对size取模
        // 理论上这个值会无限增大到Integer.MAX_VALUE,然后+1溢出后变为Integer.MIN_VALUE.
        // 这对轮询算法实际上无所谓。
        return nextServerCyclicCounter.getAndIncrement() % size;
    }
}
