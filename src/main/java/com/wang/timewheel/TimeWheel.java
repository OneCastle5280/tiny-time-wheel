package com.wang.timewheel;

import java.util.concurrent.TimeUnit;

/**
 * @author wangjiabao
 */
public interface TimeWheel {

    /**
     * 定时任务
     *
     * @param timeTask 任务
     * @param delay    延迟时间
     * @param timeUnit 时间单位
     */
    void scheduledTask(TinyTimeTask timeTask, long delay, TimeUnit timeUnit);

    /**
     * 停止
     */
    void stop();

    /**
     * 执行定时任务
     *
     * @param taskHolder
     */
    void runTask(TinyTimeTaskHolder taskHolder);

}
