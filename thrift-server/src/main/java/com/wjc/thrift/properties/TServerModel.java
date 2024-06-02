package com.wjc.thrift.properties;

/**
 * @author wjc
 * @date 2024-04-06 17:24
 * @desription
 */
public class TServerModel {
    /**
     * 服务模型 - 单线程阻塞式
     */
    public static final String SERVER_MODEL_SIMPLE = "simple";

    /**
     * 服务模型 - 单线程非阻塞式
     */
    public static final String SERVER_MODEL_NON_BLOCKING = "nonBlocking";

    /**
     * 服务模型 - 线程池
     */
    public static final String SERVER_MODEL_THREAD_POOL = "threadPool";

    /**
     * 服务模型 - 半同步半异步
     */
    public static final String SERVER_MODEL_HS_HA = "hsHa";

    /**
     * 服务模型 - 线程池选择器
     */
    public static final String SERVER_MODEL_THREADED_SELECTOR = "threadedSelector";

    /**
     * 默认的服务模型
     */
    public static final String SERVER_MODEL_DEFAULT = SERVER_MODEL_HS_HA;
}
