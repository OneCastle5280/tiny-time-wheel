package com.wang.timewheel;

/**
 * @author wangjiabao
 */
public interface TimeWheel {

    /**
     * 添加任务
     *
     * @param task
     */
    void addTask(TinyTask task);

    /**
     * 启动时间轮
     */
    void start();
}
