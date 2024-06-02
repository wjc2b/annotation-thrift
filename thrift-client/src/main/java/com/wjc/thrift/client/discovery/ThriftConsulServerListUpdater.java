package com.wjc.thrift.client.discovery;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wjc
 * @date 2024-04-17 19:44
 * @desription 服务端更新组件。具体的：设置了一个定时任务，用于从Consul Server中定时更新。
 */
public class ThriftConsulServerListUpdater implements ServerListUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftConsulServerListUpdater.class);

    private final AtomicBoolean isActive = new AtomicBoolean(false);

    private final long initialDelayMs;
    private final long refreshIntervalMs;

    private volatile ScheduledFuture<?> scheduledFuture;
    public ThriftConsulServerListUpdater() {
        this(30000);
    }
    public ThriftConsulServerListUpdater(long refreshIntervalMs) {
        this(0, refreshIntervalMs);
    }
    public ThriftConsulServerListUpdater(long initialDelayMs, long refreshIntervalMs) {
        this.initialDelayMs = initialDelayMs;
        this.refreshIntervalMs = refreshIntervalMs;
    }

    private static class LazyHolder{
        private static final int CORE_THREAD = 2;
        private static Thread shutdownThread;

        static ScheduledThreadPoolExecutor serverListRefreshExecutor;

        static {
            ThreadFactory factory = new ThreadFactoryBuilder()
                    .setNameFormat("ThriftConsulServerListUpdater-%d")
                    .setDaemon(true)
                    .build();
            // 创建一个2个核心的线程，定时监听ServerList的更新
            serverListRefreshExecutor = new ScheduledThreadPoolExecutor(CORE_THREAD, factory);
            // 创建一个关闭线程池的线程，用于在JVM停止时关闭线程池。
            shutdownThread = new Thread(() -> {
                LOGGER.info("Shutting down the Executor Pool for ThriftConsulServerListUpdater");
                shutdownExecutorPool();
            });
            // 添加到addShutdownHook()中。
            Runtime.getRuntime().addShutdownHook(shutdownThread);
        }

        private static void shutdownExecutorPool() {
            if (serverListRefreshExecutor!=null){
                serverListRefreshExecutor.shutdown();
                if (shutdownThread!=null){
                    try{
                        Runtime.getRuntime().removeShutdownHook(shutdownThread);
                    }catch (IllegalStateException e){
                        LOGGER.error("Failed to shutdown the Executor Pool for ThriftConsulServerListUpdater", e);
                    }
                }
            }
        }
    }

    private static ScheduledThreadPoolExecutor getRefreshExecutor(){return LazyHolder.serverListRefreshExecutor;}

    @Override
    public synchronized void start(UpdateAction updateAction) {
        // 仅start一次
        if (isActive.compareAndSet(false,true)){
            Runnable scheduledRunnable = () ->{
                if (!isActive.get()){ // false
                    if (scheduledFuture!=null){
                        scheduledFuture.cancel(true);
                    }
                    return;
                }
                try {
                    // 更新操作
                    updateAction.doUpdate();
                }catch (Exception e){
                    LOGGER.warn("Failed one do update action", e);
                }
            };
            // 将Runnable线程放入Future中
            scheduledFuture = getRefreshExecutor().scheduleWithFixedDelay(
                    scheduledRunnable,
                    initialDelayMs,
                    refreshIntervalMs,
                    TimeUnit.MILLISECONDS
            );
        }else {
            LOGGER.info("Already active, no other operation");
        }
    }

    @Override
    public void stop() {
        if (isActive.compareAndSet(true,false)){
            if (scheduledFuture!=null){
                scheduledFuture.cancel(true);
            }
        }else {
            LOGGER.info("Not active, no other operation");
        }
    }
}
