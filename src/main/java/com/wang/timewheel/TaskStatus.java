package com.wang.timewheel;

import lombok.Getter;

/**
 * @author wangjiabao
 */

@Getter
public enum TaskStatus {

    /**
     * 等待中
     */
    WAIT(0),
    /**
     * 已执行
     */
    EXECUTED(1),


    ;
    private final Integer value;

    TaskStatus(final Integer value) {
        this.value = value;
    }
}
