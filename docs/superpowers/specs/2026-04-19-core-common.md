# 需求设计：scor-core common 基础设施层
> 日期: 2026-04-20  
模块: scorpion-core  
包路径: `com.scorpion.common`
>

---

## 1. 概述
### 1.1 背景
scorpion-core 是整个 Scorpion SDK 的基础核心模块，处于分层架构的 Layer 2，所有上层功能模块（scorpion-config、scorpion-spring、scorpion-utils、scorpion-debug）都依赖它。其中 `common` 包承担了**基础设施**职责，需要提供统一的异常体系、RPC 模板、工具类和常量定义。

目前 `common` 包的实现尚未完成，需要从零设计并实现。

### 1.2 目标
1. **统一异常体系** — 定义 `ResultCode` 接口 + `CommonException` 基类 + 分类子异常 + 断言工具
2. **RPC 模板方法** — 提供 `FacadeTemplate` 模板，以非 AOP 方式统一处理参数校验、异常捕获、Result 填充
3. **通用工具类** — 日期转换、环境判断、AOP 代理解包、Thrift 序列化过滤、高性能反射 Getter
4. **基础常量** — 字符分隔符常量、文档链接常量

### 1.3 设计原则
+ **零外部框架依赖**（exception、constants 包）：非 Spring 项目也能使用
+ **最小依赖**：仅依赖 commons-lang3、Guava、FastJSON、Thrift（按需）
+ **不可实例化**：工具类用 `abstract class`，常量类用 `final class + private constructor`
+ **所有异常继承 **`CommonException`：保证异常体系的一致性

---

## 2. 整体架构
```plain
com.scorpion.common
├── constants/                    ← 常量定义（无依赖）
│   ├── Constants                 ← 字符分隔符常量
│   └── DocumentConstants         ← 文档链接常量
├── exception/                    ← 异常体系（仅依赖 commons-lang3）
│   ├── ResultCode                ← 错误码接口
│   ├── CommonResultCodeEnum      ← 通用错误码枚举
│   ├── CommonException           ← 通用异常基类
│   ├── DownstreamException       ← 下游调用异常
│   ├── ParamsInvalidException    ← 参数校验异常
│   ├── SystemLogicException      ← 系统逻辑异常
│   └── AssertUtils               ← 断言工具类
├── rpc/                          ← RPC 模板（依赖 exception + infra-rpc）
│   ├── RpcExecutor               ← RPC 执行器接口
│   ├── FacadeCallback            ← Facade 回调接口
│   └── FacadeTemplate            ← Facade 模板方法
└── utils/                        ← 工具类
    ├── DateUtils                 ← 日期转换
    ├── EnvUtils                  ← 环境判断
    ├── AopUtils                  ← AOP 代理解包（依赖 Spring AOP）
    ├── ThriftPropertyFilter      ← Thrift 序列化过滤（依赖 FastJSON + Thrift）
    └── invoke/                   ← 高性能反射
        ├── BindGetter            ← Getter 绑定接口
        └── BindGetters           ← Getter 绑定实现（LambdaMetafactory）
```

**依赖方向**（自底向上）：

```plain
constants（无依赖）
    ↑
exception（仅 commons-lang3）
    ↑
rpc（exception + infra-rpc-base）
utils（各自独立的外部依赖）
```

---

## 3. 详细设计
### 3.1 常量定义 — `constants`
#### 3.1.1 `Constants`
字符分隔符常量，避免业务代码中硬编码 magic string。

| 常量 | 值 | 用途 |
| --- | --- | --- |
| `EMPTY` | `""` | 空字符串 |
| `COMMA` | `","` | 逗号分隔 |
| `POINT` | `"."` | 点号（包路径、域名） |
| `STAR` | `"*"` | 通配符 |
| `SLASH` / `BACK_SLASH` | `"/"` / `"\\"` | 路径分隔 |
| `UNDERLINE` | `"_"` | 下划线连接 |
| `SEMICOLON` / `COLON` | `";"` / `":"` | 分隔符 |
| `PLUS` / `MINUS` | `"+"` / `"-"` | 运算符号 |


#### 3.1.2 `DocumentConstants`
SDK 文档链接常量，用于异常提示信息中引导用户查看文档。

| 常量 | 用途 |
| --- | --- |
| `CONFIG_DOC` | 配置能力使用文档 |
| `CONFIG_VALIDATOR_DOC` | 自定义配置校验文档 |


---

### 3.2 异常体系 — `exception`
#### 3.2.1 `ResultCode` 接口
统一错误码的顶层抽象，所有错误码枚举必须实现此接口。

```java
public interface ResultCode {
    int getCode();              // 错误码数值，透传到 RPC Result.code
    String getMessage();        // 错误信息，透传到 RPC Result.message
    boolean isRetryable();      // 是否可重试（供调用方判断重试策略）
    String getCodeDescription();// 错误码文字描述（仅用于监控打点，默认返回 name()）
}
```

**设计决策**：

+ 用接口而非抽象类 — 业务方的错误码枚举可以自由实现，不受继承限制
+ `isRetryable()` — 让错误码自带重试语义，调用方无需 hardcode 判断
+ `getCodeDescription()` — 与 `getMessage()` 分离，前者给监控系统用（固定），后者给用户看（可自定义）

#### 3.2.2 `CommonResultCodeEnum`
SDK 通用错误码枚举，业务方可定义自己的枚举实现 `ResultCode`。

| code | 常量 | 含义 | 可重试 |
| --- | --- | --- | --- |
| 0 | `SUCCESS` | 成功 | false |
| 10000 | `SYSTEM_ERROR` | 系统异常兜底 | true |
| 10001 | `PARAMETER_ILLEGAL` | 参数非法 | false |
| 10002 | `INTEGRATION_ERROR` | 下游异常 | true |
| 10003 | `STORE_ERROR` | 存储异常 | true |
| 10004 | `CONCURRENT_UPDATE` | 并发更新 | true |
| 10005 | `BILL_DUPLICATE` | 流水重复 | false |
| 10006 | `CONFIG_ERROR` | 配置异常 | true |
| 10007 | `CONFIG_NOT_EXIST` | 配置不存在 | true |
| 10008 | `LOCK_FAIL` | 加锁失败 | true |
| 10009 | `AUTH_FAIL` | 鉴权失败 | true |


**设计决策**：

+ 错误码从 10000 起步，0 保留给 SUCCESS，预留 1~9999 给业务自定义
+ `retryable` 属性让下游调用方能快速判断是否值得重试

#### 3.2.3 `CommonException`
统一异常基类，所有业务异常都继承此类。

```java
public class CommonException extends RuntimeException {
    private int code;
    private String message;
    private boolean retryable;
    private String codeDescription;
}
```

**构造方法矩阵**：

| 构造方法 | code 来源 | message 来源 |
| --- | --- | --- |
| `(ResultCode)` | resultCode.getCode() | resultCode.getMessage() |
| `(String message)` | SYSTEM_ERROR.getCode() | 自定义 message |
| `(ResultCode, String message)` | resultCode.getCode() | 自定义 message |
| `(ResultCode, String message, Throwable cause)` | resultCode.getCode() | 自定义 message |
| `(int, String, boolean, String, Throwable)` | 直接指定 | 直接指定 |


**设计决策**：

+ 继承 `RuntimeException` 而非 checked exception — 避免 throws 声明污染
+ `code` 用 `int` 基本类型 — 与 RPC Result.code 对齐，避免 NPE
+ 只传 `String message` 时默认使用 `SYSTEM_ERROR` 错误码

#### 3.2.4 分类子异常
| 子异常 | 语义 | 典型场景 |
| --- | --- | --- |
| `ParamsInvalidException` | 参数校验失败 | 入参为空、格式不合法 |
| `DownstreamException` | 下游调用失败 | DB 超时、Redis 不可用、RPC 异常 |
| `SystemLogicException` | 系统逻辑错误 | 不可能到达的分支、数据不一致 |


三个子异常的构造方法与 `CommonException` 完全一致，仅用于 `catch` 时的类型区分。

#### 3.2.5 `AssertUtils`
断言工具类，统一参数校验风格。

```java
public abstract class AssertUtils {
    // 核心方法 — 所有其他方法最终委托到这里
    public static void isTrue(boolean expression, ResultCode resultCode)
    public static void isTrue(boolean expression, ResultCode resultCode, String message)
}
```

**方法清单（13 对，每对含 ResultCode / ResultCode+message 两个重载）**：

| 方法 | 判断逻辑 | 依赖 |
| --- | --- | --- |
| `isTrue` / `isFalse` | 布尔表达式 | — |
| `isNull` / `isNotNull` | `== null` | — |
| `isBlank` / `isNotBlank` | `StringUtils.isBlank()` | commons-lang3 |
| `isNotEmpty(T[])` | `array != null && length > 0` | — |
| `isNotEmpty(Collection)` | `!= null && !isEmpty()` | — |
| `isNotEmpty(Map)` | `!= null && !isEmpty()` | — |
| `isEquals` / `isNotEquals` | `Objects.equals()` / `ObjectUtils.notEqual()` | commons-lang3 |


**设计决策**：

+ 类声明为 `abstract` — 防止实例化
+ 所有方法委托 `isTrue()` — 单一控制点，异常构造逻辑集中
+ `resultCode` 参数用 `Objects.requireNonNull()` 校验 — 防止断言本身的参数为空

---

### 3.3 RPC 模板 — `rpc`
#### 3.3.1 `RpcExecutor<REQ, RES>`
RPC 调用的函数式接口，用于封装下游调用。

```java
@FunctionalInterface
public interface RpcExecutor<REQ, RES> {
    RES execute(Context context, REQ req) throws TException;
}
```

#### 3.3.2 `FacadeCallback<REQ, RES>`
Facade 方法的回调接口，业务方实现此接口来定义具体的处理逻辑。

```java
public interface FacadeCallback<REQ, RES> {
    String identifier();                        // 方法标识（用于日志前缀）
    default void checkParameters(REQ request) {} // 参数校验（默认空）
    RES execute(REQ request);                    // 业务逻辑
}
```

#### 3.3.3 `FacadeTemplate`
非 AOP 方式的 RPC 门面模板方法。将参数校验、业务执行、异常捕获、Result 填充等公共逻辑抽取到模板中。

**执行流程**：

```plain
execute(request, callback)
    │
    ├── 1. 反射获取 RES 类型的 Class
    ├── 2. callback.checkParameters(request)    ← 参数校验
    ├── 3. callback.execute(request)            ← 业务逻辑
    │
    ├── catch CommonException
    │   └── 填充 Result(code, message, retryable)
    ├── catch DuplicateKeyException
    │   └── 填充 Result(STORE_ERROR)
    └── catch Exception
        ├── execute()       → 填充 Result(SYSTEM_ERROR) + 原始堆栈信息
        └── executeForFE()  → 填充 Result(SYSTEM_ERROR) + "系统异常"（隐藏堆栈）
```

**设计决策**：

+ `execute()` vs `executeForFE()` — 前者返回完整异常信息（后端调试），后者只返回"系统异常"（前端展示）
+ 单独捕获 `DuplicateKeyException` — DB 唯一键冲突是高频异常，统一转为 `STORE_ERROR`
+ 通过反射获取 RES 类型并实例化空 Result — 避免业务方手动创建错误响应

---

### 3.4 工具类 — `utils`
#### 3.4.1 `DateUtils`
日期格式化/解析工具，使用 `FastDateFormat`（线程安全）替代 `SimpleDateFormat`。

| 格式常量 | 格式 | 示例 |
| --- | --- | --- |
| `DATE_PATTERN_COMPLETE` | `yyyy-MM-dd HH:mm:ss SSS` | `2026-04-20 21:00:00 123` |
| `DATE_PATTERN_COMMON` | `yyyy-MM-dd HH:mm:ss` | `2026-04-20 21:00:00` |
| `DATE_PATTERN_SHORT_DATE1` | `yyyyMMdd` | `20260420` |
| `DATE_PATTERN_SHORT_DATE2` | `yyyy-MM-dd` | `2026-04-20` |


默认格式为 `DATE_PATTERN_COMMON`。

**设计决策**：使用 commons-lang3 的 `FastDateFormat` 而非 JDK 的 `SimpleDateFormat`，因为后者非线程安全。

#### 3.4.2 `EnvUtils`
运行环境判断工具，支持三种环境变量来源：

```plain
优先级: System.getProperty("env") > System.getenv("ENV") > System.getenv("XHS_ENV")
```

| 方法 | 匹配环境值 |
| --- | --- |
| `isDev()` | `dev` |
| `isSit()` | `sit` |
| `isBeta()` | `beta`, `staging` |
| `isProd()` | `prod`, `production` |
| `isUnknown()` | 以上都不匹配 |


**设计决策**：环境值在类加载时静态初始化（`static {}` 块），全局只计算一次。

#### 3.4.3 `AopUtils`
继承 Spring 的 `org.springframework.aop.support.AopUtils`，增加获取目标对象的方法。

```java
public static <T> T getTargetObject(Object candidate)
```

递归解包 `Advised` 代理，支持多重代理嵌套，直到获取到真实的目标对象。

#### 3.4.4 `ThriftPropertyFilter`
FastJSON 序列化过滤器，跳过 Thrift 自动生成的 `setXXX` 字段。

+ 仅对 `TBase` 子类生效
+ 跳过所有以 `set` 开头的字段名（Thrift 生成的位标记字段）
+ 使用单例模式 `INSTANCE`

#### 3.4.5 `BindGetter` / `BindGetters`
高性能反射 Getter 绑定，使用 `LambdaMetafactory` 在首次调用时生成等价于直接方法调用的 lambda。

```java
public interface BindGetter<T, V> {
    V get(T target);

    static <T, V> BindGetter<T, V> constant(V value);              // 常量值
    static <T, V> BindGetter<T, V> field(Class<T>, String, Class<V>); // 字段 getter
    static <T, V> BindGetter<T, V> fieldOrMethod(Class<T>, String, Class<V>); // 字段或方法
}
```

`BindGetters`** 实现要点**：

+ `field()` — 优先查找 `getXxx()` / `isXxx()`（boolean），找不到则直接用 `MethodHandle` 读字段
+ `fieldOrMethod()` — 先尝试作为无参方法调用，失败则 fallback 到 `field()`
+ `lambda()` — 使用 `LambdaMetafactory.metafactory()` 将 `MethodHandle` 转为 `BindGetter` lambda，性能等同于直接方法调用

---

## 4. 约束
+ Java 版本：1.8，不使用更高版本 API
+ `exception` 和 `constants` 包不得依赖 Spring 框架
+ `AopUtils` 依赖 Spring AOP 是允许的（scorpion-core 本身有 Spring 依赖）
+ 所有工具类方法必须是 `static`，无状态
