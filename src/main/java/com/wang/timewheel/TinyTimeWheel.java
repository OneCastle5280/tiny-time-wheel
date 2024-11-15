package com.wang.timewheel;


/**
 * @author wangjiabao
 */
public class TinyTimeWheel implements TimeWheel{

    /**
     * 时间槽的数量
     */
    private final Integer slotCount;
    /**
     * 时间槽的间隔时间，单位: ms
     */
    private final Integer slotInterval;
    /**
     * 时间轮一圈表示的时间，单位: ms
     */
    private final Long cycleTime;
    /**
     * 时间轮指针
     */
    private Integer currentSlotIndex;

    /**
     * 时间轮本体; 将指定时间切分多个时间槽
     */
    private TinyTimeWheelSlot[] timeWheel;

    /**
     * 时间指针
     */
    private final TimePointer timePointer;


    public TinyTimeWheel(int slotCount, int slotInterval) {
        this.slotCount = slotCount;
        this.slotInterval = slotInterval;
        this.cycleTime = (long) slotCount * slotInterval;

        timeWheel = new TinyTimeWheelSlot[slotCount];
        for (int i = 0; i < slotCount; i++) {
            timeWheel[i] = new TinyTimeWheelSlot();
        }

        // 指针
        timePointer = new TinyTimePointer();
    }

    @Override
    public void addTask(TinyTask task) {
        if (task.getDelayTime() > this.cycleTime) {
            // 超过时间轮最大能表示的时间 todo
        }
        // 计算 slotIndex
        int slotIndex = this.getSlotIndex(task.getDelayTime());
        // 加入到队列
        timeWheel[slotIndex].addTask(task);
    }

    @Override
    public void start() {
        // 启动一个线程
       new Thread(() -> {
            while (true) {
                // 获取当前时间槽
                TinyTimeWheelSlot currentSlot = timeWheel[currentSlotIndex];
                // 获取时
                TinyTask task = currentSlot.pollTask();
                // 执行任务
                task.run();
                // 推进指针
                timePointer.tick();
            }
        }).start();
    }

    private int getSlotIndex(long delayTime) {
        int slotIndex = (int) delayTime / slotInterval;
        // todo 超过最大时间
        return this.currentSlotIndex + slotIndex;
    }
}
