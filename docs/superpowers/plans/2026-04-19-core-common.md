# 实施计划：scorpion-core common 基础设施层
> 日期: 2026-04-20  
关联需求: [specs/2026-04-20--scorpion-core-common.md](../specs/2026-04-20--scorpion-core-common.md)
>

---

## 实施原则
1. **自底向上**: 先实现无依赖的常量和接口，再实现依赖它们的工具类和模板
2. **异常体系优先**: exception 包是所有其他包的基础
3. **每步可测**: 每个 Phase 完成后可独立运行单元测试
4. **零破坏**: scorpion-core 无内部模块依赖，不存在兼容性风险

---

## Phase 1 — 常量定义
**预计工时**: 0.5 小时

| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 1.1 | 实现 `Constants` 字符分隔符常量类 | `common/constants/Constants.java` | ⬜ |
| 1.2 | 实现 `DocumentConstants` 文档链接常量类 | `common/constants/DocumentConstants.java` | ⬜ |


**规范**:

+ `Constants` 使用 `public final class` + `private constructor` 防止实例化
+ 所有常量为 `public static final String`
+ `DocumentConstants` 存放 SDK 文档的 HTTPS 链接

**验证点**: 编译通过，常量值可正常引用。

---

## Phase 2 — 异常体系
**预计工时**: 1 天

本阶段是整个 common 包的核心地基，后续的 RPC 模板和断言工具都依赖异常体系。

### Step 2.1 — 错误码接口与枚举
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 2.1.1 | 定义 `ResultCode` 接口（4 个方法） | `common/exception/ResultCode.java` | ⬜ |
| 2.1.2 | 实现 `CommonResultCodeEnum`（11 个枚举值） | `common/exception/CommonResultCodeEnum.java` | ⬜ |


**关键决策**:

+ `ResultCode` 是接口而非抽象类，允许业务方自定义错误码枚举
+ `getCodeDescription()` 提供 default 实现，返回枚举 `name()`
+ 枚举构造参数：`(int code, String message, boolean retryable)`

### Step 2.2 — 异常类层次
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 2.2.1 | 实现 `CommonException`（5 个构造方法） | `common/exception/CommonException.java` | ⬜ |
| 2.2.2 | 实现 `ParamsInvalidException` | `common/exception/ParamsInvalidException.java` | ⬜ |
| 2.2.3 | 实现 `DownstreamException` | `common/exception/DownstreamException.java` | ⬜ |
| 2.2.4 | 实现 `SystemLogicException` | `common/exception/SystemLogicException.java` | ⬜ |


**关键决策**:

+ `CommonException` 继承 `RuntimeException`，非 checked exception
+ `code` 用 `int` 基本类型，与 RPC Result.code 对齐
+ 只传 `String message` 的构造方法默认使用 `SYSTEM_ERROR` 错误码
+ 三个子异常的构造方法与 `CommonException` 完全一致，仅用于 catch 时类型区分

### Step 2.3 — 断言工具
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 2.3.1 | 实现 `AssertUtils`（13 对断言方法） | `common/exception/AssertUtils.java` | ⬜ |


**关键决策**:

+ 类声明为 `abstract`（防止实例化）
+ 所有方法最终委托 `isTrue(boolean, ResultCode)` 和 `isTrue(boolean, ResultCode, String)`
+ `resultCode` 参数用 `Objects.requireNonNull()` 先行校验
+ 字符串判断使用 `StringUtils.isBlank()`（commons-lang3）
+ 相等判断使用 `Objects.equals()` 和 `ObjectUtils.notEqual()`（commons-lang3）

### Step 2.4 — 异常体系单元测试
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 2.4.1 | `CommonResultCodeEnum` 枚举值和属性测试 | `test/common/exception/CommonResultCodeEnumTest.java` | ⬜ |
| 2.4.2 | `CommonException` 构造方法测试（5 种构造） | `test/common/exception/CommonExceptionTest.java` | ⬜ |
| 2.4.3 | 子异常类型区分测试 | `test/common/exception/SubExceptionTest.java` | ⬜ |
| 2.4.4 | `AssertUtils` 每个方法的成功/失败路径测试 | `test/common/exception/AssertUtilsTest.java` | ⬜ |
| 2.4.5 | `AssertUtils` resultCode 为 null 时 NPE 测试 | `test/common/exception/AssertUtilsTest.java` | ⬜ |


**验证点**:

+ 每个枚举值的 `code`/`message`/`retryable` 正确
+ `CommonException(ResultCode)` 正确提取所有字段
+ `CommonException(String)` 使用 `SYSTEM_ERROR` 作为默认 code
+ `catch (ParamsInvalidException)` 能区分于 `catch (DownstreamException)`
+ `AssertUtils.isNotNull(null, code)` 抛出 `CommonException`
+ `AssertUtils.isNotNull("obj", code)` 不抛异常
+ `AssertUtils.isTrue(false, null)` 抛出 NPE

---

## Phase 3 — RPC 模板
**预计工时**: 1 天

### Step 3.1 — 接口定义
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 3.1.1 | 定义 `RpcExecutor<REQ, RES>` 函数式接口 | `common/rpc/RpcExecutor.java` | ⬜ |
| 3.1.2 | 定义 `FacadeCallback<REQ, RES>` 回调接口 | `common/rpc/FacadeCallback.java` | ⬜ |


**关键决策**:

+ `RpcExecutor` 标注 `@FunctionalInterface`，支持 lambda 传参
+ `FacadeCallback.checkParameters()` 提供空 default 实现，业务方可选覆写
+ `FacadeCallback.identifier()` 返回方法标识符，用于日志前缀

### Step 3.2 — 模板方法实现
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 3.2.1 | 实现 `FacadeTemplate`（execute + executeForFE） | `common/rpc/FacadeTemplate.java` | ⬜ |


**需实现的方法**:

| 方法 | 未知异常处理 | 适用场景 |
| --- | --- | --- |
| `execute(request, callback)` | 返回原始异常堆栈信息 | 后端服务间调用 |
| `executeForFE(request, callback)` | 返回"系统异常"（隐藏堆栈） | 面向前端的接口 |


**异常处理矩阵**:

| 异常类型 | code | message | retryable |
| --- | --- | --- | --- |
| `CommonException` | e.getCode() | e.getMessage() | e.isRetryable() |
| `DuplicateKeyException` | STORE_ERROR | "数据重复" | true |
| `Exception`（execute） | SYSTEM_ERROR | 原始异常信息 | true |
| `Exception`（executeForFE） | SYSTEM_ERROR | "系统异常" | true |


### Step 3.3 — RPC 模板单元测试
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 3.3.1 | `FacadeTemplate.execute()` 正常路径测试 | `test/common/rpc/FacadeTemplateTest.java` | ⬜ |
| 3.3.2 | `FacadeTemplate.execute()` CommonException 路径测试 | `test/common/rpc/FacadeTemplateTest.java` | ⬜ |
| 3.3.3 | `FacadeTemplate.execute()` 未知异常路径测试 | `test/common/rpc/FacadeTemplateTest.java` | ⬜ |
| 3.3.4 | `FacadeTemplate.executeForFE()` 未知异常隐藏堆栈测试 | `test/common/rpc/FacadeTemplateTest.java` | ⬜ |
| 3.3.5 | `FacadeTemplate` DuplicateKeyException 转换测试 | `test/common/rpc/FacadeTemplateTest.java` | ⬜ |


**验证点**:

+ 正常执行：callback.execute() 的返回值正确透传
+ CommonException：code 和 message 正确填充到 Result
+ 未知异常 execute()：Result.message 包含原始异常信息
+ 未知异常 executeForFE()：Result.message 为"系统异常"，不暴露堆栈
+ DuplicateKeyException：转换为 STORE_ERROR

---

## Phase 4 — 工具类
**预计工时**: 1 天

### Step 4.1 — 日期工具
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 4.1.1 | 实现 `DateUtils`（4 种格式 + format/parse 方法） | `common/utils/DateUtils.java` | ⬜ |


**关键决策**:

+ 使用 `FastDateFormat`（commons-lang3），线程安全
+ 默认格式为 `yyyy-MM-dd HH:mm:ss`
+ `formatDate(null)` 返回 null，`parseDate(null)` 返回 null

### Step 4.2 — 环境工具
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 4.2.1 | 实现 `EnvUtils`（环境检测 + 5 个判断方法） | `common/utils/EnvUtils.java` | ⬜ |


**关键决策**:

+ 环境值在 `static {}` 块中初始化，全局只计算一次
+ 检测优先级：`System.getProperty("env")` > `System.getenv("ENV")` > `System.getenv("XHS_ENV")`
+ 忽略大小写匹配（toLowerCase）

### Step 4.3 — AOP 与序列化工具
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 4.3.1 | 实现 `AopUtils`（继承 Spring AopUtils + getTargetObject） | `common/utils/AopUtils.java` | ⬜ |
| 4.3.2 | 实现 `ThriftPropertyFilter`（FastJSON 序列化过滤） | `common/utils/ThriftPropertyFilter.java` | ⬜ |


**关键决策**:

+ `AopUtils.getTargetObject()` 递归解包，支持多重代理嵌套
+ `ThriftPropertyFilter` 单例模式（`static final INSTANCE`），仅对 `TBase` 子类过滤 `set` 前缀字段

### Step 4.4 — 高性能反射 Getter
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 4.4.1 | 定义 `BindGetter<T, V>` 函数式接口 + 静态工厂方法 | `common/utils/invoke/BindGetter.java` | ⬜ |
| 4.4.2 | 实现 `BindGetters`（LambdaMetafactory 动态生成 lambda） | `common/utils/invoke/BindGetters.java` | ⬜ |


**关键决策**:

+ `BindGetter` 是 `@FunctionalInterface`，支持 lambda 赋值
+ `BindGetters` 为 package-private 类，不对外暴露
+ `field()` 查找顺序：`getXxx()` → `isXxx()`（仅 boolean）→ 直接字段访问
+ `lambda()` 使用 `LambdaMetafactory.metafactory()` 生成 lambda，性能等同直接方法调用

### Step 4.5 — 工具类单元测试
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 4.5.1 | `DateUtils` format/parse 正常路径 + null 边界测试 | `test/common/utils/DateUtilsTest.java` | ⬜ |
| 4.5.2 | `EnvUtils` 各环境判断测试 | `test/common/utils/EnvUtilsTest.java` | ⬜ |
| 4.5.3 | `BindGetter` field/method/constant 测试 | `test/common/utils/BindGetterTest.java` | ⬜ |


**验证点**:

+ `DateUtils.formatDate(date)` 输出正确格式字符串
+ `DateUtils.parseDate("2026-04-20 21:00:00")` 解析正确
+ `DateUtils.formatDate(null)` 返回 null
+ `BindGetter.field()` 能正确读取 POJO 属性
+ `BindGetter.constant("v")` 始终返回 `"v"`

---

## 实施顺序总览
```plain
Phase 1 (0.5h)     Phase 2 (1d)          Phase 3 (1d)       Phase 4 (1d)
─────────────       ────────────          ────────────       ────────────
Constants      →    ResultCode       →    RpcExecutor   →    DateUtils
DocumentConst       CommonResultCode      FacadeCallback     EnvUtils
                    CommonException       FacadeTemplate     AopUtils
                    Sub-Exceptions                           ThriftFilter
                    AssertUtils                              BindGetter
                    Tests                 Tests              Tests
```

**总预计工时**: 3.5 天

# 验证
```
1. 基于上述执行生成的部分进行一下验证，看下是否有错误，将错误进行修改；
2. 检查哪些是合理的，哪些是不合理的。将不合理的地方进行修改。
3. 哪些设计是不合理的，将不合理的地方进行修改
```
