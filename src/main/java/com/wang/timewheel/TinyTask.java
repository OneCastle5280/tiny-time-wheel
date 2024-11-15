package com.wang.timewheel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.TimerTask;

/**
 * @author wangjiabao
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public abstract class TinyTask extends TimerTask {
    /**
     * 任务名称
     */
    private String name;

    /**
     * 周期数
     */
    private Integer cycleCount;

    /**
     * 任务延迟时间
     */
    private Long delayTime;
}
