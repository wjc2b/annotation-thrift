package com.wjc.thrift.client.loadbalance;


import com.wjc.thrift.client.common.ThriftServerNode;

public interface IRule {

    ThriftServerNode choose(String key);

    void setLoadBalancer(ILoadBalancer lb);

    ILoadBalancer getLoadBalancer();

}
