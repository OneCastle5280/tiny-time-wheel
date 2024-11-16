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


}
