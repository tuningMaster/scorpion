# 规则：异常处理
> 适用范围：所有模块
>

## 规则
+ 业务异常统一使用 `CommonException` 或其子类
+ 错误码参考 `CommonResultCodeEnum`，新增错误码需在枚举中统一定义
+ 对外接口必须捕获所有异常并转换为 `CommonException`

## 错误码规范
| 错误码 | 常量 | 含义 |
| --- | --- | --- |
| `0` | SUCCESS | 成功 |
| `10000` | SYSTEM_ERROR | 系统异常兜底 |
| `10001` | PARAMETER_ILLEGAL | 参数非法 |
| `10002` | LOCK_FAIL | 加锁失败 |
| `10003` | IDEMPOTENT | 幂等拦截 |


## 正确示例
```java
// ✅ 对外接口统一异常处理
public CommonResult<Void> doSomething(Request request) {
    try {
        // 业务逻辑
    } catch (CommonException e) {
        return CommonResult.fail(e.getCode(), e.getMessage());
    } catch (Exception e) {
        log.error("unexpected error", e);
        return CommonResult.fail(CommonResultCodeEnum.SYSTEM_ERROR);
    }
}
```

## 禁止
+ 禁止吞掉异常（空 catch 块）
+ 禁止在非入口层抛出 RuntimeException 的原始子类（如 `IllegalArgumentException`），应包装为 `CommonException`
