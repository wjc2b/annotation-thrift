package com.wjc.thrift.client.loadbalance;

/**
 * @author wjc
 * @date 2024-04-17 16:55
 * @desription
 */
public abstract class AbstractLoadBalancerRule implements IRule{
    protected ILoadBalancer lb;

    @Override
    public void setLoadBalancer(ILoadBalancer lb) {
        this.lb = lb;
    }

    @Override
    public ILoadBalancer getLoadBalancer() {
        return lb;
    }
}
