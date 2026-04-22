# 规则：单例模式
> 适用范围：所有需要单例的类
>

## 规则
必须使用**静态内部类 Holder 模式**，禁止使用双重检查锁（DCL）。

## 正确示例
```java
public class XxxService {
    private static final class InstanceHolder {
        static final XxxService INSTANCE = new XxxService();
    }

    public static XxxService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private XxxService() {}
}
```

## 禁止示例
```java
// ❌ DCL — 依赖 volatile，容易漏写，无额外收益
private static volatile XxxService INSTANCE;

public static XxxService getInstance() {
    if (INSTANCE == null) {
        synchronized (XxxService.class) {
            if (INSTANCE == null) {
                INSTANCE = new XxxService();
            }
        }
    }
    return INSTANCE;
}
```

## 原因
+ Holder 模式的线程安全由 JVM 类加载机制保证，不可能写错
+ 天然懒加载 — 只有首次调用 `getInstance()` 时才触发内部类加载
+ 无锁，零性能开销
+ 项目已有实践：`ConfigServiceHolder`、`ConfigHolder`

## 例外
如果单例构造需要外部参数（如运行时配置），可使用带 `volatile` 的 DCL，但需在代码注释中说明原因。
