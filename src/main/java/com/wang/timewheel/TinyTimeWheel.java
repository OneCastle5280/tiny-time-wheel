package com.wang.timewheel;

import java.util.LinkedList;
import java.util.concurrent.*;

/**
 * tiny 时间轮
 *
 * @author wangjiabao
 */
public class TinyTimeWheel implements TimeWheel{

    public static final String TINY_TIME_WHEEL = "tiny-time-wheel";

    /**
     * 启动线程池
     */
    private final ExecutorService startThreadPool;

    /**
     * 时间轮本体
     */
    private final TinyTimeWheelBucket[] wheel;

    /**
     * 时间间隔
     */
    private final Long duration;

    /**
     * 时间轮 bucket 数量
     */
    private final Integer bucketNum;
    /**
     * 时间轮启动时间，后续运行则根据该时间进行计算
     */
    private final Long startTime;

    /**
     * 指针当前指向的 bucket 位置
     */
    private Integer currentBucketIndex;


    public TinyTimeWheel (Long duration, Integer bucketNum) {
        this.duration = duration;
        this.bucketNum = bucketNum;
        wheel = new TinyTimeWheelBucket[bucketNum];
        for (int i = 0; i < bucketNum; i++) {
            wheel[i] = new TinyTimeWheelBucket();
        }

        // 时间轮启动时间
        this.startTime = System.currentTimeMillis();

        // 自定义线程池启动线程
        this.startThreadPool = new ThreadPoolExecutor(
                1,
                1,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> new Thread(r, TINY_TIME_WHEEL)
        );


        // 启动时间轮
        this.startThreadPool.submit(this::tick);
    }

    /**
     * 模拟指针执行
     */
    private void tick() {
        while (true) {
            // 执行当前 bucket 中的所有任务
            this.executeCurrentBucketTask();
            // 指针等待
            tickWait();
            // 指针向前
            this.currentBucketIndex ++;
        }
    }

    private void tickWait() {
        // 计算出下一次执行的时间
        long nextExecuteTime = startTime + (currentBucketIndex + 1) * this.duration;
        // 判断是否休眠等待
        long sleepTime = nextExecuteTime - System.currentTimeMillis();
        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                // 忽略异常，继续执行
            }
        }
    }

    private void executeCurrentBucketTask() {
        LinkedList<TinyTimeTaskHolder> taskList = wheel[this.currentBucketIndex].getTaskList();
        if (taskList != null && !taskList.isEmpty()) {
            while (true) {
                TinyTimeTaskHolder taskHolder = taskList.poll();
                if (taskHolder == null) {
                    break;
                }
                // 执行任务
                taskHolder.getTimeTask().run();
            }
        }
    }



    @Override
    public void scheduledTask(TinyTimeTask timeTask, long delay, TimeUnit timeUnit) {
        TinyTimeTaskHolder taskHolder = new TinyTimeTaskHolder();
        taskHolder.setTimeTask(timeTask);
        taskHolder.setExecuteTime(timeUnit.toMillis(duration) + System.currentTimeMillis());

        if (delay <= 0) {
            this.runTask(taskHolder);
        } else {
            // 将任务加入到时间轮
            this.addToWheel(taskHolder);
        }
    }

    @Override
    public void addToWheel(TinyTimeTaskHolder taskHolder) {
        this.getBucket(taskHolder).getTaskList().add(taskHolder);
    }

    @Override
    public void runTask(TinyTimeTaskHolder taskHolder) {
        // todo 考虑线程池
        taskHolder.getTimeTask().run();
    }

    @Override
    public TinyTimeWheelBucket getBucket(TinyTimeTaskHolder taskHolder) {
        //
        // todo 计算获取时间槽
        return wheel[0];
    }
}
