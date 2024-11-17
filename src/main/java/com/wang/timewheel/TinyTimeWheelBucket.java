package com.wang.timewheel;

import lombok.Data;

import java.util.LinkedList;

/**
 * 时间槽
 *
 * @author wangjiabao
 */
@Data
public class TinyTimeWheelBucket {
    /**
     * bucket 中的任务列表
     */
    private LinkedList<TinyTimeTaskHolder> taskList;

    public TinyTimeWheelBucket() {
        this.taskList = new LinkedList<>();
    }

    /**
     * 执行任务
     */
    public void executeTasks() {
        for (TinyTimeTaskHolder taskHolder : this.taskList) {
            if (taskHolder == null) {
                continue;
            }
            if (taskHolder.isExecuted()) {
                // 任务已执行
                continue;
            }
            if (taskHolder.getRounds() > 0) {
                // 圈数减 1, 等待下一次执行
                taskHolder.setRounds(taskHolder.getRounds() - 1);
                continue;
            }
            // 执行任务
            TinyTimeTask timeTask = taskHolder.getTimeTask();
            timeTask.run();
            // 更新任务状态
            taskHolder.setStatus(TaskStatus.EXECUTED.getValue());
        }
    }

}
