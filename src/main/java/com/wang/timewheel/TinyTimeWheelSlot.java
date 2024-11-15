package com.wang.timewheel;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedList;

/**
 * @author wangjiabao
 */
@Data
@Accessors(chain = true)
public class TinyTimeWheelSlot {

    /**
     * 默认队列最大容量
     */
    private static final Integer DEFAULT_QUEUE_MAX_SIZE = 1024;

    /**
     * 队列最大容量
     */
    private Integer queueMaxSize;

    /**
     * 定时任务队列, 存储定时任务
     */
    private LinkedList<TinyTask> taskQueue;

    public TinyTimeWheelSlot() {
        this(DEFAULT_QUEUE_MAX_SIZE);
    }

    public TinyTimeWheelSlot(Integer queueMaxSize) {
        this.queueMaxSize = queueMaxSize;
        this.taskQueue = new LinkedList<>();
    }

    public void addTask(TinyTask task) {
        if (taskQueue.size() >= queueMaxSize) {
            throw new RuntimeException("task queue is full");
        }
        taskQueue.add(task);
    }

    public TinyTask pollTask() {
        return taskQueue.poll();
    }
}
