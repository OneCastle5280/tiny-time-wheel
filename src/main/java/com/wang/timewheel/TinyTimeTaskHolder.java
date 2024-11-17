package com.wang.timewheel;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 封装 TinyTimeTask
 *
 * @author wangjiabao
 */
@Data
@Accessors(chain = true)
public class TinyTimeTaskHolder {

    /**
     * 定时任务
     */
    private TinyTimeTask timeTask;

    /**
     * 任务延迟时间
     */
    private Long executeTime;

    /**
     * 周期数
     */
    private Integer rounds;

    /**
     * 任务状态 {@link TaskStatus}
     */
    private Integer status;

    public TinyTimeTaskHolder(TinyTimeTask timeTask, Long executeTime, Integer rounds) {
        this.timeTask = timeTask;
        this.executeTime = executeTime;
        this.rounds = rounds;
        this.status = TaskStatus.WAIT.getValue();
    }

    public boolean isExecuted() {
        return TaskStatus.EXECUTED.getValue().equals(this.status);
    }
}
