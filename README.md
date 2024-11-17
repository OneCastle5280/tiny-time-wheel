# tiny-time-wheel
`tiny-time-wheel` 是一个用 Java 实现的单层时间轮算法。

## 1. 快速使用
使用实例
```java
    public static void main(String[] args) throws InterruptedException {
        // 创建一个时间间隔为 100ms，10 个时间槽的时间轮
        TinyTimeWheel tinyTimeWheel = new TinyTimeWheel(100, TimeUnit.MILLISECONDS,10);
        
        long startTime = System.currentTimeMillis();
        
        // 加入 300ms 之后执行的任务
        tinyTimeWheel.scheduledTask(new TinyTimeTask() {
            @Override
            public void run() {
                long cost = System.currentTimeMillis() - startTime;
                System.out.println("after 300ms task, cost: "+ cost + "ms");
            }
        }, 300, TimeUnit.MILLISECONDS);

        // 加入 100ms 之后执行的任务
        tinyTimeWheel.scheduledTask(new TinyTimeTask() {
            @Override
            public void run() {
                long cost = System.currentTimeMillis() - startTime;
                System.out.println("after 100ms task, cost: "+ cost + "ms");
            }
        }, 100, TimeUnit.MILLISECONDS);
        Thread.sleep(1000);
        // 时间轮停止
        tinyTimeWheel.stop();
    }
```
数据结果
```
after 100ms task, cost: 105ms
after 300ms task, cost: 302ms
```

## 2. 实现思路
