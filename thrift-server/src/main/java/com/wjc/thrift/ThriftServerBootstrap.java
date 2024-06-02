package com.wjc.thrift;

import org.apache.thrift.server.TServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wjc
 * @date 2024-04-10 20:27
 * @desription
 */
public class ThriftServerBootstrap implements SmartLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftServerBootstrap.class);

    private ThriftServiceGroup thriftServiceGroup;
    public ThriftServerBootstrap(ThriftServiceGroup thriftServiceGroup) {
        this.thriftServiceGroup = thriftServiceGroup;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void start() {
        if(CollectionUtils.isEmpty(thriftServiceGroup.getServers())){
            return;
        }
        LOGGER.info("Starting thrift servers");
        AtomicInteger serverIndex = new AtomicInteger(0);
        thriftServiceGroup.getServers().forEach(server ->{
            ThriftRunner runner = new ThriftRunner(server);
            new Thread(runner,"thrift-server-"+serverIndex.incrementAndGet()).start();
        });
    }

    @Override
    public void stop(Runnable callback) {
        if (isRunning()){
            LOGGER.info("Shutting down thrift servers");
            thriftServiceGroup.getServers().forEach(server ->{
                server.setShouldStop(true);
                server.stop();
            });
            callback.run();
        }
    }

    @Override
    public void stop() {
        stop(null);
    }

    @Override
    public boolean isRunning() {
        return thriftServiceGroup.getServers().stream().anyMatch(TServer::isServing);
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    private static class ThriftRunner implements Runnable{
        // 内部线程类，用来跑一个线程
        private static final Logger LOGGER = LoggerFactory.getLogger(ThriftRunner.class);

        private TServer server;

        public ThriftRunner(TServer server) {
            this.server = server;
        }

        @Override
        public void run() {
            if (server!=null){
                this.server.serve();
                LOGGER.info(server.isServing() ? "Thrift server started successfully" : "Thrift server failed to start");
            }
        }
    }

}
