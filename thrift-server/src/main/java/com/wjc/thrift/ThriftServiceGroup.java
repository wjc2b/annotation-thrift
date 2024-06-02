package com.wjc.thrift;

import org.apache.thrift.server.TServer;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author wjc
 * @date 2024-04-10 20:19
 * @desription 将所有注册完的Server加入Group
 */
public class ThriftServiceGroup {
    private Queue<TServer> servers = new LinkedBlockingDeque<>();
    public ThriftServiceGroup(TServer... tServer) {
        if (Objects.isNull(tServer) || tServer.length==0){
            return;
        }
        this.servers.addAll(Arrays.asList(tServer));
    }

    public ThriftServiceGroup(List<TServer> servers){
        if (CollectionUtils.isEmpty(servers)){
            return;
        }
        this.servers.clear();
        this.servers.addAll(servers);
    }

    public Queue<TServer> getServers() {
        return servers;
    }

    public void setServers(Queue<TServer> servers) {
        this.servers = servers;
    }
}
