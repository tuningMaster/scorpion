# 规则：并发编程
> 适用范围：涉及多线程、缓存、定时任务的代码
>

## 规则
### 不可变集合
内存缓存必须使用 Guava `ImmutableSortedMap` / `ImmutableMap`，通过**引用替换**实现无锁更新。

```java
// ✅ 正确 — 引用替换，读取无锁
private volatile ImmutableSortedMap<String, Config> configMap = ImmutableSortedMap.of();

public void reload(List<Config> newConfigs) {
    this.configMap = ImmutableSortedMap.copyOf(buildMap(newConfigs));
}

public Config get(String key) {
    return configMap.get(key); // 无锁读取
}
```

```java
// ❌ 禁止 — ConcurrentHashMap + 逐条更新（非原子性，中间状态可见）
```

### 定时任务
使用 `ScheduledThreadPoolExecutor`，禁止使用 `Timer`。

```java
// ✅ 正确
ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1, 
    new ThreadFactoryBuilder().setNameFormat("reload-%d").setDaemon(true).build());
scheduler.scheduleWithFixedDelay(this::reload, 0, interval, TimeUnit.MILLISECONDS);
```

```java
// ❌ 禁止 — Timer 单线程，一个任务异常会导致所有任务停止
new Timer().schedule(task, 0, interval);
```

### 线程池
+ 禁止使用 `Executors.newXxx()` 快捷方法（无界队列风险）
+ 必须显式指定队列大小和拒绝策略
+ 线程命名必须有业务含义
