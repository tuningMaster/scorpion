# 规则：单元测试编写
> 适用范围：所有新增/修改代码
>

## 规则
### 测试位置
+ 所有测试代码集中在 `crab-test` 模块
+ Spring 环境测试必须继承 `AbstractSpringTest`

### 命名规范
+ 测试类：`XxxTest.java`
+ 测试方法：`test_方法名_场景描述()`

```java
// ✅ 正确
public class ConfigConverterTest {
    @Test
    public void test_do2Dto_nullInput() { ... }
    
    @Test
    public void test_do2Dto_emptyStrategyConfigs() { ... }
}
```

### 测试分层
| 类型 | 依赖 Spring | 基类 | 适用场景 |
| --- | --- | --- | --- |
| 单元测试 | 否 | 无 | 纯逻辑：工具类、转换器、枚举 |
| 集成测试 | 是 | `AbstractSpringTest` | 需要 Bean 注入、AOP 切面 |


### Mock 数据
+ Mock 类放在对应模块的 `mock/` 子目录下
+ 使用 Mockito 进行 Mock，静态方法使用 PowerMock

### 断言
+ 优先使用 JUnit Assert
+ 复杂断言可使用 AssertJ

### 测试框架版本
| 框架 | 版本 |
| --- | --- |
| JUnit 4 | 4.13.2 |
| TestNG | 6.9.4 |
| Mockito | 2.28.2 |
| PowerMock | 2.0.9 |


## 覆盖率目标
| 模块 | 目标 | 优先级 |
| --- | --- | --- |
| crab-core | ≥ 80% | P0 |
| crab-config | ≥ 85% | P0 |
| crab-spring | ≥ 80% | P0 |
| crab-utils | ≥ 70% | P1 |
| crab-debug | ≥ 75% | P1 |

