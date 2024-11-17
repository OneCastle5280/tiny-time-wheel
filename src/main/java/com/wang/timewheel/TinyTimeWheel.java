package com.wang.timewheel;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * tiny 时间轮
 *
 * @author wangjiabao
 */
public class TinyTimeWheel implements TimeWheel{

    /**
     * 默认从队列中获取 10w 个任务到时间轮中
     */
    public static final int BATCH_ADD_TO_WHEEL_SIZE = 100000;

    /**
     * 时间轮本体
     */
    private final TinyTimeWheelBucket[] wheel;

    /**
     * 时间间隔
     */
    private final Long duration;

    /**
     * 验码，方便计算 mod
     */
    private final Integer mask;
    /**
     * 时间轮启动时间，后续运行则根据该时间进行计算
     */
    private Long startTime;

    /**
     * 停止信息
     */
    private boolean stop;

    /**
     * 服务启动线程池
     */
    private final ExecutorService startThreadPool;

    /**
     * 定时任务队列
     */
    private final Queue<TinyTimeTaskHolder> timeoutQueue;

    /**
     * 构建时间轮
     *
     * @param duration      间隔，单位 ms
     * @param bucketNum     时间槽数量
     */
    public TinyTimeWheel (long duration, TimeUnit unit, int bucketNum) {
        this.duration = unit.toMillis(duration);
        bucketNum = this.normalizeWheelBucketNum(bucketNum);
        this.mask = bucketNum - 1;

        // 队列
        this.timeoutQueue = new LinkedBlockingQueue<>();

        // 时间轮本体
        wheel = new TinyTimeWheelBucket[bucketNum];
        for (int i = 0; i < bucketNum; i++) {
            wheel[i] = new TinyTimeWheelBucket();
        }

        // 自定义线程池启动线程
        this.startThreadPool = new ThreadPoolExecutor(
                1,
                1,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> new Thread(r, "tiny-time-wheel")
        );

        // 启动时间轮
        startThreadPool.submit(new TickWorker());
    }

    /**
     * 初始化时间轮数量，让数量为 2 的倍数
     *
     * @param bucketNum
     * @return
     */
    private int normalizeWheelBucketNum(int bucketNum) {
        // 这里参考java8 hashmap的算法，使推算的过程固定
        int n = bucketNum - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        // 这里1073741824 = 2^30,防止溢出
        return (n < 0) ? 1 : (n >= 1073741824) ? 1073741824 : n + 1;
    }


    private class TickWorker implements Runnable {

        /**
         * 具体时间轮开始时间有 tick 个 duration，即距离开始时间的的绝对长度
         */
        private int tick;

        @Override
        public void run() {
            startTime = System.currentTimeMillis();
            while (!stop) {
                // 先将任务从队列中取出推入到时间轮中
                pushTaskToWheelFromQueue();
                // 执行任务
                executeBucketTask();
                // 等待指针移动
                waitTick();
                // 指针向前走
                tick++;
            }
        }

        /**
         * 计算 bucket index
         *
         * @param tick
         * @return
         */
        private int getBucketIndex(int tick) {
            return tick & mask;
        }

        /**
         * 等待下一个时间间隔, 并且返回当前与 startTime 相差的时间
         *
         * @return
         */
        private void waitTick() {
            // 计算下一次执行的绝对时间
            long nextExecuteTime = (this.tick + 1) * duration + startTime;
            long sleepTime = nextExecuteTime - System.currentTimeMillis();
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    // 出现了中断异常
                }
            }
        }

        /**
         * 将任务从队列中加入到时间轮
         */
        private void pushTaskToWheelFromQueue() {
            // 每次从队列取出 10w 个任务
            for (int i = 0; i < BATCH_ADD_TO_WHEEL_SIZE; i++) {
                TinyTimeTaskHolder taskHolder = timeoutQueue.poll();
                if (taskHolder == null) {
                    break;
                }
                // 计算任务执行时间需要具体多少次时针移动才能到达
                long offset = taskHolder.getExecuteTime() - startTime;
                int taskTickCount = (int) (offset / duration);
                if (taskTickCount < tick) {
                    // 说明任务在队列呆着已经超过了真正的执行时间，直接加入到当前 bucket，立即执行
                    wheel[getBucketIndex(tick)].getTaskList().add(taskHolder);
                } else {
                    // 计算轮数
                    int rounds = (taskTickCount - tick) / wheel.length;
                    taskHolder.setRounds(rounds);
                    wheel[getBucketIndex(taskTickCount)].getTaskList().add(taskHolder);
                }
            }
        }

        /**
         * 执行时间轮中的定时任务
         */
        private void executeBucketTask() {
            wheel[this.getBucketIndex(tick)].executeTasks();
        }
    }


    @Override
    public void scheduledTask(TinyTimeTask timeTask, long delay, TimeUnit timeUnit) {
        TinyTimeTaskHolder taskHolder = new TinyTimeTaskHolder(timeTask, timeUnit.toMillis(delay) + System.currentTimeMillis(), 0);

        if (delay <= 0) {
            this.runTask(taskHolder);
        } else {
            // 先讲任务加入到队列中，解耦
            this.timeoutQueue.add(taskHolder);
        }
    }

    @Override
    public void runTask(TinyTimeTaskHolder taskHolder) {
        if (taskHolder.getRounds() > 0) {
            taskHolder.setRounds(taskHolder.getRounds() - 1);
        } else {
            taskHolder.getTimeTask().run();
        }
    }

    @Override
    public void stop() {
        this.stop = true;
        this.startThreadPool.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        TinyTimeWheel tinyTimeWheel = new TinyTimeWheel(100, TimeUnit.MILLISECONDS,10);
        for (int i = 0; i < 1000; i++) {
            final int taskId = i;
            long startTime = System.currentTimeMillis();
            tinyTimeWheel.scheduledTask(new TinyTimeTask() {
                @Override
                public void run() {
                    long cost = System.currentTimeMillis() - startTime;
                    System.out.println("after 300ms task: " + taskId + ", cost: "+ cost + "ms");
                }
            }, 100, TimeUnit.MILLISECONDS);
        }
        Thread.sleep(30000);
        tinyTimeWheel.stop();
    }
}
