package com.wjc.thrift.client.discovery;

/**
 * @author wjc
 * @date 2024-04-17 19:41
 * @desription
 */
public interface ServerListUpdater {
    /**
     * 创建了一个函数式接口，Java8新特性
     * 只有一个抽象方法的接口。
     * 这里虽然没有显示声明为抽象方法，但是：在接口中所有的方法默认都是公共的抽象的，所以即便没有显式地使用 abstract 关键字，它仍然是抽象的。
     * 可以用 @FunctionalInterface 注解来判断是否是一个函数式接口
     *
     * 函数式接口可以用来和lambda表达式打comba。
     *
     */
    @FunctionalInterface
    public interface UpdateAction{
        void doUpdate();
    }
    void start(UpdateAction updateAction);
    void stop();
}
