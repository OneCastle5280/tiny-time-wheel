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
     * 任务实际执行时间
     */
    private Long executeTime;
}
