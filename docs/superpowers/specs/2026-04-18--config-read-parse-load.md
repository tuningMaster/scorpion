# 需求设计：scorpion-config 配置读取、解析与内存加载
> 日期: 2026-04-17  
状态: 待开发  
模块: scorpion-config  
负责人: tuningMaster
>

---

## 1. 背景与目标
### 1.1 背景
Scorpion SDK 的核心能力之一是**配置管理**。业务方需要在应用中读取配置平台上维护的各类配置（运营活动策略、灰度开关、切流规则等），并将其加载到 JVM 内存中以实现高性能读取。

目前 scorpion-config 模块已完成注解定义（`@Config`、`@ConfigIndex`），但**数据模型（ConfigDTO、ConfigDO、StrategyConfigDTO 等）尚未设计**，**配置从数据源读取 → 解析为 Java 对象 → 加载到内存 → 定时重载**的完整链路也尚未实现。

### 1.2 目标
实现 scorpion-config 模块的配置生命周期管理，包括：

1. **数据模型设计** — 设计配置的数据库模型、传输模型、策略模型和枚举体系
2. **配置描述扫描** — 编译时扫描 `@Config` 注解，运行时初始化配置元信息
3. **配置数据读取** — 从多种数据源（DB / 远程服务）读取配置数据
4. **配置内容解析** — 将 JSON/自定义格式内容解析为 Java POJO
5. **灰度策略匹配** — 根据用户参数匹配白名单/百分比/BETA 等策略
6. **内存配置管理** — 构建带索引的内存配置集合，支持高效查询
7. **定时重载机制** — 后台线程定时拉取最新配置，无锁替换内存数据
8. **配置变更回调** — 支持业务方监听配置变更事件
9. **切流工具** — 基于配置的新老逻辑切流能力

---

## 2. 整体架构
### 2.1 全链路数据流
```plain
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          配置生命周期全链路                                        │
│                                                                                 │
│  ┌──────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────────┐   │
│  │ 数据源    │    │ ConfigLoad   │    │ ConfigDesc   │    │ MemoryApp        │   │
│  │ (DB/RPC) │───▶│ Service      │───▶│ riptor       │───▶│ Configs          │   │
│  │          │    │ 加载+备份     │    │ 解析+转换     │    │ 内存索引集合       │   │
│  └──────────┘    └──────────────┘    └──────────────┘    └──────────────────┘   │
│       ▲                                                        │                │
│       │                                                        ▼                │
│       │           ┌──────────────┐    ┌──────────────┐    ┌─────────────┐       │
│       │           │ Scheduled    │    │ ConfigHolder │    │ Switchers   │       │
│       └───────────│ Reload       │    │ (静态API)    │    │ (切流API)   │       │
│                   │ 定时重载      │    │ findById     │    │ call/hit    │       │
│                   └──────────────┘    │ findAll      │    │ miss/callRun│       │
│                                       │ findByIndex  │    └─────────────┘       │
│                                       └──────────────┘                          │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 核心类关系
```plain
ConfigEngine (引擎，编排初始化流程)
  ├─ ConfigDescriptors (配置元信息注册表)
  │   └─ ConfigDescriptor<C> (单个配置类型描述)
  │       ├─ ConfigDecoder<C> (解码器: JSONDecoder / 自定义)
  │       └─ ConfigIndexDescriptor<C> (索引描述)
  │
  ├─ ConfigLoadService (配置加载服务)
  │   └─ List<ConfigProvider> (有序数据源列表)
  │       ├─ DatabaseConfigProvider (DB 数据源)
  │       └─ ... (可扩展: RPC 数据源等)
  │
  ├─ MemoryConfigService (内存配置服务)
  │   └─ MemoryAppConfigs (单应用配置快照)
  │       ├─ idMap: SortedMap<MemoryConfigIndex, MemoryConfig<?>>
  │       ├─ indexMap: SortedMap<MemoryConfigIndex, MemoryConfig<?>>
  │       └─ unKnownMap: SortedMap<MemoryConfigIndex, MemoryConfig<JSONObject>>
  │
  └─ ConfigCallbackFactory (变更回调工厂)
      └─ List<ConfigChangeCallback<C>> (业务回调列表)
```

---

## 3. 详细设计
### 3.1 配置描述扫描（编译时 + 运行时）
#### 3.1.1 编译时扫描
`@Config` 注解标记了 `@CompileScanMeta(resourceFile = "META-INF/scorpion/config-types.idx")`，编译时由 `CompileScanMetaAnnotationProcessor` 自动收集所有标注了 `@Config` 的类全限定名，写入资源文件。

**输入**: 业务方的 `@Config` 类（如下示例）

```java
@Config(type = "activity_config", idField = "activityId", indices = {
    @ConfigIndex(type = "city", key = "cityCode")
})
@Data
public class ActivityConfig {
    private String activityId;
    private String name;
    private String cityCode;
    private boolean enabled;
}
```

**输出**: `META-INF/scorpion/config-types.idx` 文件，内容为类名集合的 JSON。

#### 3.1.2 运行时初始化 — `ConfigDescriptors`
`ConfigDescriptors.init()` 在引擎启动时调用：

1. 通过 `ResourcesAccessor` 加载 `config-types.idx` 资源文件
2. 反射获取每个类的 `Class` 对象
3. 为每个类创建 `ConfigDescriptor<C>` 实例
4. 构建两个不可变映射：`configType → descriptor`、`class → descriptor`
5. 校验 type 唯一性，重复则抛异常

**关键约束**:

+ configType 在全局必须唯一
+ `ConfigDescriptor` 构造时解析 `@Config` 注解的所有属性
+ decoder 特殊处理：如果是默认的 `JSONDecoder`，需要传入 `idField` 参数

#### 3.1.3 配置描述 — `ConfigDescriptor<C>`
每个 `@Config` 类对应一个 `ConfigDescriptor`，封装：

| 字段 | 来源 | 说明 |
| --- | --- | --- |
| `configClazz` | `Class<C>` | 配置类型 |
| `type` | `@Config.type()` | 配置类型标识 |
| `decoder` | `@Config.decoder()` | 解码器实例（默认 JSONDecoder） |
| `indices` | `@Config.indices()` | 索引描述映射 `indexType → ConfigIndexDescriptor` |


核心方法:

+ `toMemory(ConfigDTO)` — DTO → 内存模型，包含策略配置解析
+ `decode(id, content)` — 内容解析，异常返回 null 不中断
+ `decodeStrategyItem(id, StrategyConfigDTO)` — 策略配置项解析
+ `decodeDefaultItem(ConfigDTO)` — 默认配置项解析

#### 3.1.4 索引描述 — `ConfigIndexDescriptor<C>`
每个 `@ConfigIndex` 对应一个索引描述：

| 字段 | 来源 | 说明 |
| --- | --- | --- |
| `indexType` | `@ConfigIndex.type()` | 索引类型标识 |
| `indexKeyGetter` | `@ConfigIndex.key()` | 通过 `BindGetter` 反射获取字段值的 getter |


`toIndex()` 方法将配置对象转换为 `MemoryConfigIndex`，当 indexKey 为空时返回 null（该条数据不建索引）。

---

### 3.2 配置数据读取 — `ConfigProvider` 多源加载
#### 3.2.1 接口设计
```java
public interface ConfigProvider {
    // 优先级，越小越高
    default int order() { return Integer.MAX_VALUE; }

    // 加载全部 ONLINE 配置。有新数据返回 AppAllConfigDTO，否则 null，失败抛异常
    AppAllConfigDTO loadAppAllConfigs(long currentVersion);

    // 可选：备份配置
    default void backupAppAllConfigs(AppAllConfigDTO appAllConfig) {}
}
```

#### 3.2.2 数据库实现 — `DatabaseConfigProvider`
+ 调用 `ConfigRepository.listAllOnlineConfig()` 读取所有 `status = 'ONLINE'` 的配置
+ 通过 `ConfigConverter.do2Dto()` 将 DO 转换为 DTO
+ 版本号简单递增 `currentVersion + 1`

#### 3.2.3 加载服务 — `ConfigLoadService`
`loadAndBackup(currentVersion, currentCount)` 方法实现多源容灾：

```plain
按 order 顺序遍历 ConfigProvider 列表:
  ├─ provider.loadAppAllConfigs(currentVersion)
  │   ├─ 返回非 null → 执行 backupAppAllConfigs → 返回结果
  │   ├─ 返回 null   → 跳到下一个 provider
  │   └─ 抛异常      → 记录 error 日志 → 跳到下一个 provider
  └─ 全部失败 → 返回 null
```

**设计要点**: 多个 provider 按优先级排序，先从主数据源加载，失败时自动降级到备份数据源。

---

### 3.3 配置内容解析 — `ConfigDecoder`
#### 3.3.1 解码器接口
```java
public interface ConfigDecoder<T> {
    T decode(String id, String content, Class<T> clazz);
}
```

**约定**: 如果 `content == null`，`ConfigDescriptor.decode()` 直接返回 null，不调用 decoder。

#### 3.3.2 JSON 解码器 — `JSONDecoder<T>`
默认实现，处理流程：

1. `JSON.parseObject(content)` 解析为 `JSONObject`
2. 如果 `idField` 非空，将配置 id 注入到 JSON 的对应字段
3. `json.toJavaObject(clazz)` 转换为目标类型

**设计亮点**: 配置平台上的配置内容中通常不包含 id 字段（id 是平台层面的概念），通过 `idField` 机制自动将 id 注入到 Java 对象中。

#### 3.3.3 自定义解码器
业务方可实现 `ConfigDecoder` 接口：

```java
@Config(type = "custom", idField = "id", decoder = MyCustomDecoder.class)
public class CustomConfig { ... }
```

自定义 decoder 通过 `newInstance()` 实例化（需要无参构造函数）。

---

### 3.4 灰度策略匹配
#### 3.4.1 策略接口
```java
public interface Strategy {
    boolean match(String param);
}
```

#### 3.4.2 策略类型
| 类型 | 实现类 | 匹配规则 |
| --- | --- | --- |
| `WHITE_LIST` | `WhiteListStrategy` | `param` 在逗号分隔的白名单中 |
| `PERCENT` | `PercentStrategy` | `abs(param.hashCode() % 100) < percent`（短 key 会 repeat 3 次） |
| `BETA` | `BetaStrategy` | 固定匹配所有（单例） |
| 默认 | `DefaultStrategy` | 固定匹配所有（兜底策略，单例） |
| 无 | `NoneStrategy` | 固定不匹配（策略解析失败时使用，单例） |
| 组合 | `GroupStrategy` | 多条策略 AND 组合 |


#### 3.4.3 策略解析 — `Strategy.parse()`
```plain
输入: List<StrategyDTO>
  ├─ 列表为空 → 抛异常
  ├─ 仅 1 条 → parseOne(dto) 返回对应 Strategy
  └─ 多条     → 逐个 parseOne → 包装为 GroupStrategy
```

`parseOne()` 根据 `strategyType` 字段匹配 `StrategyTypeEnum`，构造对应的 Strategy 实例。

---

### 3.5 内存配置管理
#### 3.5.1 内存数据结构
`MemoryConfig<T>` — 单条配置的内存表示：

```java
@Value @Builder
public class MemoryConfig<T> {
    String type;                                // 配置类型
    String id;                                  // 配置 id
    List<MemoryStrategyConfig<T>> strategyConfigs;  // 策略配置列表
    MemoryConfigSelector<T> configSelector;      // 配置选择器
}
```

`MemoryStrategyConfig<T>` — 策略配置项：

```java
@Value @Builder
public class MemoryStrategyConfig<T> {
    Strategy strategy;          // 灰度策略
    T config;                   // 配置内容
    Set<MemoryConfigIndex> indices;  // 索引集合
}
```

`MemoryConfigIndex` — 统一索引键（实现 Comparable，用于 SortedMap）：

```java
@Value @Builder
public class MemoryConfigIndex implements Comparable<MemoryConfigIndex> {
    String configType;   // 配置类型
    String indexType;    // 索引类型（id 查询时为 null）
    String indexKey;     // 索引值（id 查询时为 null）
    String configId;     // 配置 id
}
```

排序规则: `configType → indexType → indexKey → configId`，null 排在最前。

#### 3.5.2 索引设计 — `MemoryAppConfigs`
`MemoryAppConfigs` 维护三个 `ImmutableSortedMap`：

| Map | Key 结构 | 用途 |
| --- | --- | --- |
| `idMap` | `{configType, null, null, configId}` | 按 id 查询 |
| `indexMap` | `{configType, indexType, indexKey, configId}` | 按索引查询 |
| `unKnownMap` | `{configType, null, null, configId}` | 未注册类型（前端用） |


**SortedMap 前缀查询**: 利用 `subMap(start, end)` 实现前缀匹配，通过构造上下界 `Range`：

+ `prefixMatchRange(configType)` — 查找某类型的所有配置
+ `prefixMatchRange(configType, indexType, indexKey)` — 索引查询

#### 3.5.3 配置选择器 — `MemoryConfigSelector`
`MemoryConfigSelector.build()` 生成一个 lambda，执行逻辑：

```plain
遍历 strategyConfigs（按优先级排列）:
  ├─ strategy.match(param) == true
  │   ├─ 非索引查询（filterIndexRange == null）→ 直接返回 config
  │   └─ 索引查询 → 检查 indices 是否在范围内
  │       ├─ 命中 → 返回 config
  │       └─ 未命中 → 返回 null（不继续匹配下一个策略）
  └─ strategy.match(param) == false → 继续下一个策略
全部不匹配 → 返回 null
```

**关键设计**: 策略列表最后一个必定是 `DefaultStrategy`（始终匹配），确保有兜底。

#### 3.5.4 查询 API — `ConfigHolder`
| 方法 | 说明 |
| --- | --- |
| `findById(class, param, id)` | 按 id 精确查询 |
| `findByIds(class, param, ids)` | 按 id 列表批量查询 |
| `findAll(class, param)` | 查询某类型全部配置 |
| `findAllByIndex(class, param, indexType, indexKey)` | 按索引查询 |


所有方法共同逻辑：

1. `ConfigDescriptors.getConfigDescriptorRequired(clazz)` 获取描述（不存在直接抛异常提示接入问题）
2. 委托 `MemoryAppConfigs` 对应方法执行查询
3. 灰度参数 `param` 传入 `configSelector.select()` 进行策略匹配

**注意**: 返回的配置实例是内存共享的，**不可修改内容**（减少 GC）。返回的 List 是新建的，可编辑。

---

### 3.6 定时重载机制
#### 3.6.1 `MemoryConfigService` 初始化流程
```plain
构造函数:
  1. 创建空的 MemoryAppConfigs（version = -1）
  2. 创建 ScheduledThreadPoolExecutor（1 线程，命名 config-reload-pool-*）
  3. 立即执行一次 reloadAppConfigs(init=true)
  4. 注册定时任务: 间隔 reloadInterval 毫秒，首次延迟随机 0~20 秒
  5. 将 this 赋值给 ConfigHolder.INSTANCE
```

#### 3.6.2 重载逻辑 — `reloadAppConfigs()`
```plain
获取当前 version 和 count
  ↓
configLoadService.loadAndBackup(currentVersion, currentCount)
  ├─ 返回非 null → 用新数据构造 MemoryAppConfigs 整体替换（无锁，引用替换）
  └─ 返回 null   → 不更新（保持旧数据）
  ↓
异常处理:
  ├─ init=true 且 appAllConfig==null → 向上抛异常（阻断启动）
  └─ 非初始化 → 100% 吃掉异常，打 error 日志（保证定时任务可重试）
```

**设计要点**:

+ **无锁替换**: 通过 `this.memoryAppConfigs = new MemoryAppConfigs(...)` 引用替换，读线程看到的是完整快照
+ **启动强依赖**: 初始化时必须成功加载配置，否则应用无法启动
+ **运行时容错**: 运行期间重载失败不影响已有配置，等待下次重试
+ **随机延迟**: 首次定时延迟 `RandomUtils.nextInt(0, 20000)` 毫秒，避免多实例同时拉取

---

### 3.7 配置变更回调 — `ConfigChangeCallback<C>`
#### 3.7.1 接口定义
```java
public interface ConfigChangeCallback<C> {
    ConfigDTO onChange(CallbackContext context, ConfigDTO configDTO);
}
```

业务方实现此接口，可以在配置变更时：

+ **校验**: 抛出 `CommonException` 会回显到配置平台前端
+ **加工**: 修改 `configDTO` 内容（如查询下游填充字段）
+ **触发**: 启动任务、刷新缓存等副作用

#### 3.7.2 便捷方法
+ `decodeDefaultConfig(configDTO)` — 解析全量配置为 Java 对象
+ `decodeStrategyConfigs(configDTO)` — 解析所有策略配置为 Java 对象列表
+ `getConfigClass()` — 通过泛型反射自动获取配置类型

---

### 3.8 切流工具 — `Switchers`
#### 3.8.1 核心方法 `call()`
```java
Switchers.call(userId, switcherId, () -> newLogic(), () -> oldLogic());
```

**执行流程**:

```plain
ConfigHolder.findById(SwitcherConfig.class, params, id)
  ├─ config == null → 执行 callOld（安全兜底）
  ├─ mode == OLD    → 执行 callOld
  ├─ mode == NEW    → 执行 callNew
  ├─ mode == BOTH   → 两者都执行，由 CheckPolicy 决策返回哪个
  └─ 其他           → 执行 callOld（兜底）
```

#### 3.8.2 便捷方法
| 方法 | 说明 |
| --- | --- |
| `hit(params, id)` | 命中切流返回 true |
| `miss(params, id)` | 未命中返回 true |
| `callRun(params, id, runNew, runOld)` | 无返回值版本 |


#### 3.8.3 监控
如果 `SwitcherConfig.monitorEnable = true`，会上报 CAT counter：

+ name: `SCORPION_SWITCHER`
+ tag: `switcherId`, `mode`

---

## 4. 数据模型设计
本章节定义配置管理涉及的所有数据模型，包括数据库层（DO）、服务传输层（DTO）、RPC 层（IDL）以及配套枚举。

### 4.1 模型分层总览
```plain
┌─────────────────────────────────────────────────────────────────┐
│                        模型分层关系                               │
│                                                                 │
│  ┌─────────────┐                                                │
│  │ ConfigItem   │  RPC 层（IDL 自动生成，对外接口传输）              │
│  │ (scorpion-idl)   │  字段全为 String/基本类型，时间为 String 格式      │
│  └──────┬───────┘                                                │
│         │ ConfigConverter.rpcModel2Do() / do2RpcModel()          │
│         ▼                                                        │
│  ┌──────────────┐                                                │
│  │  ConfigDO     │  数据库层（DAL），与数据库表一一映射               │
│  │  (impl.dal)   │  strategyConfigs 为 JSON 字符串                │
│  └──────┬────────┘                                                │
│         │ ConfigConverter.do2Dto() / dto2Do()                    │
│         ▼                                                        │
│  ┌──────────────┐                                                │
│  │  ConfigDTO    │  服务传输层（Facade），业务逻辑使用               │
│  │  (facade)     │  strategyConfigs 已解析为 List<StrategyConfigDTO>│
│  └──────────────┘                                                │
│                                                                  │
│  辅助模型:                                                        │
│  ┌──────────────────┐  ┌──────────────┐  ┌──────────────────┐    │
│  │ AppAllConfigDTO   │  │ StrategyDTO  │  │StrategyConfigDTO│    │
│  │ 全量配置包装       │  │ 单条策略规则  │  │ 策略+内容组合    │    │
│  └──────────────────┘  └──────────────┘  └──────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 数据库表设计
#### 4.2.1 表结构 DDL
```sql
CREATE TABLE config (
    id               BIGINT AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    app_name         VARCHAR(64)                        NOT NULL COMMENT '应用名(租户)',
    type             VARCHAR(64)                        NOT NULL COMMENT '配置类型',
    custom_id        VARCHAR(64)                        NOT NULL COMMENT '配置id',
    name             VARCHAR(256)                       NOT NULL COMMENT '配置名称，展示用',
    status           VARCHAR(32)                        NOT NULL COMMENT '配置状态 INIT; ONLINE; OFFLINE; DELETE',
    content          TEXT                               NULL     COMMENT '全量配置内容JSON',
    strategy_configs TEXT                               NULL     COMMENT '策略配置内容JSON',
    extra            JSON                               NULL     COMMENT '扩展信息JSON',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '数据创建时间',
    update_time      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '数据修改时间',
    CONSTRAINT uk_app_name_type_custom_id
        UNIQUE (app_name, type, custom_id)
) COMMENT '配置表';
```

#### 4.2.2 字段设计说明
| 列名 | 数据库类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK, AUTO_INCREMENT | 自增主键 |
| `app_name` | VARCHAR(64) | NOT NULL, UK | 应用名/租户标识，多租户隔离维度 |
| `type` | VARCHAR(64) | NOT NULL, UK | 配置类型，对应 `@Config.type()`，同一应用下可有多种 type |
| `custom_id` | VARCHAR(64) | NOT NULL, UK | 配置业务 id，同一 type 下唯一标识一条配置 |
| `name` | VARCHAR(256) | NOT NULL | 配置名称，配置平台前端展示用，不参与业务逻辑 |
| `status` | VARCHAR(32) | NOT NULL | 配置状态，参见 `ConfigStatusEnum` |
| `content` | TEXT | NULL | 全量配置内容，JSON 格式。为 NULL 表示配置无内容（如纯策略配置） |
| `strategy_configs` | TEXT | NULL | 策略配置 JSON，反序列化为 `List<StrategyConfigDTO>`。为 NULL 表示无灰度策略 |
| `extra` | JSON | NULL | 扩展信息，平台侧使用，业务 SDK 不消费 |
| `create_time` | DATETIME | NOT NULL, DEFAULT NOW | 记录创建时间 |
| `update_time` | DATETIME | NOT NULL, AUTO UPDATE | 记录最后修改时间 |


**唯一键**: `(app_name, type, custom_id)` — 保证同一应用、同一类型下配置 id 唯一。

**设计决策**:

+ `content` 和 `strategy_configs` 用 TEXT 而非 JSON 类型，兼容 MySQL 5.6
+ `status` 用字符串而非数字，可读性好，枚举值有限不影响性能
+ `extra` 用 JSON 类型，留给平台侧灵活扩展

### 4.3 ConfigDO — 数据库数据对象
**包路径**: `com.xiaohongshu.scorpion.config.impl.dal`  
**职责**: 与数据库表字段一一映射，属于 DAL 层内部模型。

```java
@Data
public class ConfigDO implements Serializable {
    private Long id;              // PK（包装类型，新增时为 null）
    private String appName;       // 应用名
    private String type;          // 配置类型
    private String customId;      // 配置id
    private String name;          // 配置名称
    private String status;        // 配置状态（字符串，对应 ConfigStatusEnum）
    private String content;       // 全量配置内容 JSON 字符串
    private String strategyConfigs;  // 策略配置 JSON 字符串（注意：这里是原始 String）
    private String extra;         // 扩展字段 JSON 字符串
    private Date createTime;      // 创建时间
    private Date updateTime;      // 修改时间
}
```

**关键设计点**:

+ `id` 使用 `Long`（包装类型）— 新增时可为 null，由数据库自增生成
+ `strategyConfigs` 在 DO 层保持为 **String 原始 JSON**，不在 DO 层解析，解析推迟到 DO→DTO 转换时

### 4.4 ConfigDTO — 服务传输对象
**包路径**: `com.xiaohongshu.scorpion.config.facade.model`  
**职责**: Facade 层和业务逻辑层使用的传输对象，`strategyConfigs` 已解析为结构化列表。

```java
@Data
public class ConfigDTO implements Serializable {
    private long id;              // PK（基本类型，原因见下方说明）
    private Date createTime;
    private Date updateTime;

    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,64}$")
    private String appName;       // 应用名，仅允许字母数字下划线中划线，最长64

    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,64}$")
    private String type;          // 配置类型

    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,64}$")
    private String customId;      // 配置id

    @Pattern(regexp = "^.{1,256}$")
    private String name;          // 配置名称

    @Pattern(regexp = "^(INIT|ONLINE|OFFLINE|DELETE)$")
    private String status;        // 配置状态

    @ToString.Exclude
    private String content;       // 全量配置内容（日志排除，避免大文本污染日志）

    @ToString.Exclude
    private List<StrategyConfigDTO> strategyConfigs;  // 策略配置（已解析为对象列表）

    private String extra;         // 扩展信息
}
```

**关键设计点**:

+ `id` 使用 `long`（基本类型）而非 `Long` — 因为 RPC IDL 生成的 `ConfigItem.id` 是基本类型，如果 DTO 用包装类型，`null` 拆箱到 `ConfigItem.id` 会 NPE
+ `content` 和 `strategyConfigs` 标注 `@ToString.Exclude` — 这两个字段可能包含大量 JSON 文本，排除在 toString 之外避免日志爆炸
+ 业务字段（`appName`、`type`、`customId`）使用 `@Pattern` 校验 — 限制为字母数字下划线中划线，最长 64 字符
+ `status` 用 `@Pattern` 限制枚举值 — 与 `ConfigStatusEnum` 对应

### 4.5 StrategyConfigDTO — 策略配置组合对象
**包路径**: `com.xiaohongshu.scorpion.config.facade.model`  
**职责**: 表示一组灰度策略 + 该策略下的配置内容。一条 `ConfigDTO` 可以有多个 `StrategyConfigDTO`。

```java
@Data
public class StrategyConfigDTO implements Serializable {
    @NotEmpty
    private List<StrategyDTO> strategyList;  // 策略规则列表（至少一条）

    @ToString.Exclude
    private String content;                  // 该策略下的配置内容 JSON
}
```

**语义说明**: 一条配置项的数据结构是"全量配置 + N 个策略配置"：

```plain
ConfigDTO
  ├─ content: "默认全量配置内容"          ← 所有用户的兜底配置
  └─ strategyConfigs:
      ├─ StrategyConfigDTO[0]
      │   ├─ strategyList: [{type:WHITE_LIST, whiteList:"u1,u2"}]
      │   └─ content: "白名单用户专用配置"  ← 命中白名单时使用此内容
      └─ StrategyConfigDTO[1]
          ├─ strategyList: [{type:PERCENT, percent:10}]
          └─ content: "10%灰度配置"        ← 命中10%灰度时使用此内容
```

### 4.6 StrategyDTO — 单条策略规则
**包路径**: `com.xiaohongshu.scorpion.config.facade.model`  
**职责**: 描述单条灰度策略的规则参数，字段平铺设计（非继承）。

```java
@Data
public class StrategyDTO implements Serializable {
    @NotNull
    private String strategyType;     // 策略类型（对应 StrategyTypeEnum.name()）

    private String whiteList;        // 白名单参数 — 多个值用逗号","分隔
    private Integer percent;         // 百分比参数 — 数字 0~100
}
```

**字段平铺设计决策**:

+ 不同策略类型使用不同的字段，未使用的字段为 null
+ `strategyType = "WHITE_LIST"` 时，使用 `whiteList` 字段
+ `strategyType = "PERCENT"` 时，使用 `percent` 字段
+ `strategyType = "BETA"` 时，无需额外字段
+ 优点：JSON 序列化简单、数据库存储统一、前端表单容易处理
+ 缺点：字段膨胀（新增策略类型需加字段），但策略类型有限可接受

**JSON 示例**:

```json
// 白名单策略
{"strategyType": "WHITE_LIST", "whiteList": "user1,user2,user3"}

// 百分比策略
{"strategyType": "PERCENT", "percent": 30}

// BETA 策略
{"strategyType": "BETA"}

// 组合策略（多条 StrategyDTO 组成 AND 关系）
[
  {"strategyType": "WHITE_LIST", "whiteList": "user1"},
  {"strategyType": "PERCENT", "percent": 50}
]
```

### 4.7 AppAllConfigDTO — 全量配置包装对象
**包路径**: `com.xiaohongshu.scorpion.config.facade.model`  
**职责**: 包装某个应用的全部配置数据 + 版本号，`ConfigProvider` 接口的返回值。

```java
@Data
public class AppAllConfigDTO implements Serializable {
    @Min(0)
    private long version;              // 配置版本号（递增）

    @NotNull
    private List<ConfigDTO> configList;  // 全部配置列表
}
```

**version 设计**:

+ 初始值 `-1`（表示未加载过）
+ 每次加载成功后递增
+ 用于后续支持增量加载（当前版本为全量加载，version 仅做标记）

### 4.8 枚举定义
#### 4.8.1 ConfigStatusEnum — 配置状态
```java
public enum ConfigStatusEnum {
    INIT,      // 初始化 — 新建未上线
    ONLINE,    // 上线   — 内存加载使用该状态
    OFFLINE,   // 下线   — 保留数据但不加载到内存
    DELETE     // 删除   — 逻辑删除
}
```

**状态流转**:

```plain
INIT → ONLINE → OFFLINE → DELETE
              ↘ DELETE
```

**重要**: `ConfigRepository.listAllOnlineConfig()` 仅加载 `status = 'ONLINE'` 的配置到内存。

#### 4.8.2 StrategyTypeEnum — 策略类型
```java
public enum StrategyTypeEnum {
    WHITE_LIST,   // 白名单策略
    PERCENT,      // 百分比策略
    BETA          // BETA 全量生效
}
```

提供 `findByName(String)` 静态查找方法，基于内部 `NAME_ENUM_MAP` 缓存，返回 null 表示未找到。

#### 4.8.3 ConfigChangeTypeEnum — 配置变更类型
```java
public enum ConfigChangeTypeEnum {
    WHITE_LIST("白名单变更"),   // 仅变更白名单策略
    FREEDOM("自由变更"),        // 自由编辑内容
    ROLLBACK("回滚")           // 回滚到历史版本
}
```

#### 4.8.4 OperateTypeEnum — 操作类型
```java
public enum OperateTypeEnum {
    CREATE,   // 新建配置
    UPDATE    // 修改配置
}
```

### 4.9 模型转换 — `ConfigConverter`
**包路径**: `com.xiaohongshu.scorpion.config.facade.convert`  
**职责**: 三层模型之间的转换工具类，全部为静态方法。

#### 4.9.1 转换矩阵
```plain
           ┌──────────┐
           │ConfigItem│ (RPC IDL)
           └────┬─────┘
       rpcModel2Do │ │ do2RpcModel
       rpcModel2Dto│ │ dto2RpcModel
           ┌────▼─────┐
           │ ConfigDO  │ (DAL)
           └────┬──────┘
         do2Dto │ │ dto2Do
           ┌────▼──────┐
           │ ConfigDTO  │ (Facade)
           └────────────┘
```

#### 4.9.2 关键转换逻辑
| 方向 | 关键处理 |
| --- | --- |
| DO → DTO | `strategyConfigs` 从 String **反序列化**为 `List<StrategyConfigDTO>`（FastJSON TypeReference） |
| DTO → DO | `strategyConfigs` 从 List **序列化**为 String |
| DO → RPC | `createTime`/`updateTime` 从 Date **格式化**为 String（DateUtils） |
| RPC → DO | `createTime`/`updateTime` 从 String **解析**为 Date |


`strategyConfigs`** 序列化/反序列化示例**:

```plain
数据库存储 (ConfigDO.strategyConfigs):
  "[{\"strategyList\":[{\"strategyType\":\"WHITE_LIST\",\"whiteList\":\"u1,u2\"}],\"content\":\"{...}\"}]"

反序列化为 (ConfigDTO.strategyConfigs):
  List<StrategyConfigDTO> [
    StrategyConfigDTO {
      strategyList: [StrategyDTO {strategyType: "WHITE_LIST", whiteList: "u1,u2"}],
      content: "{...}"
    }
  ]
```

#### 4.9.3 空值安全
所有转换方法遵循统一规范：

+ 入参为 `null` → 返回 `null`
+ 入参为空 List → 返回空 List（`Lists.newArrayList()`）
+ 批量转换方法委托单条转换方法 + Stream

### 4.10 数据模型关系图
```plain
┌────────────────────────────────────────────────────────────────────────────────┐
│                                                                                │
│  数据库 (config 表)                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐      │
│  │ id | app_name | type | custom_id | status | content | strategy_configs │   │
│  │ 1  | myapp    | act  | act_001   | ONLINE | {...}   | [{...}]          │   │
│  └────────────────────────────┬─────────────────────────────────────────┘      │
│                               │ JDBC 查询                                      │
│                               ▼                                                │
│  ConfigDO ─────────────────────────────────────────────────────────────        │
│  │ strategyConfigs = "[{\"strategyList\":[...],\"content\":\"...\"}]"  │        │
│  └────────────────────────────┬────────────────────────────────────────        │
│                               │ ConfigConverter.do2Dto()                       │
│                               │ (JSON.parseObject → List<StrategyConfigDTO>)  │
│                               ▼                                                │
│  ConfigDTO ────────────────────────────────────────────────────────────        │
│  │ content = "{\"name\":\"活动A\"}"                                    │        │
│  │ strategyConfigs = [                                                 │        │
│  │   StrategyConfigDTO {                                               │        │
│  │     strategyList: [StrategyDTO {type:WHITE_LIST, whiteList:"u1"}]   │        │
│  │     content: "{\"name\":\"白名单专属活动A\"}"                         │        │
│  │   }                                                                 │        │
│  │ ]                                                                   │        │
│  └────────────────────────────┬────────────────────────────────────────        │
│                               │ ConfigDescriptor.toMemory()                    │
│                               ▼                                                │
│  MemoryConfig<ActivityConfig> ──────────────────────────────────────────       │
│  │ type = "act"                                                        │       │
│  │ id = "act_001"                                                      │       │
│  │ strategyConfigs = [                                                 │       │
│  │   MemoryStrategyConfig { strategy=WhiteListStrategy, config=... }   │       │
│  │   MemoryStrategyConfig { strategy=DefaultStrategy,   config=... }   │       │
│  │ ]                                                                   │       │
│  └─────────────────────────────────────────────────────────────────────        │
│                                                                                │
└────────────────────────────────────────────────────────────────────────────────┘
```

---

## 5. 初始化编排 — `ConfigEngine`
`ConfigEngine.init()` 按顺序编排所有组件初始化：

```plain
1. ConfigServiceHolder.getInstance() 获取单例上下文
2. 校验 configRepository 非空 → 注册到上下文
3. ConfigDescriptors.init() — 扫描并注册所有 @Config 类
4. 校验 configProviderList 非空 → 按 order 排序 → 注册到上下文
5. new ConfigLoadService(providers) → 注册到上下文
6. 校验 reloadInterval > 0
7. new MemoryConfigService(reloadInterval, enableMonitor, loadService)
   └─ 内部：立即加载 + 启动定时任务 + 赋值 ConfigHolder.INSTANCE
8. 注册 configCallbackFactory
```

`ConfigEngine.shutdown()`:

```plain
1. memoryConfigService.shutdown()
   └─ 关闭线程池 + 清空内存配置
```

---

## 6. 异常与容错设计
| 场景 | 处理方式 |
| --- | --- |
| `@Config` 类编译时未扫描到 | 运行时 `getConfigDescriptorRequired()` 抛异常并提示检查 pom 接入 |
| configType 重复 | `ConfigDescriptors.init()` 抛 RuntimeException |
| 配置 JSON 解析失败 | `ConfigDescriptor.decode()` 打 error 日志，返回 null，不影响其他配置 |
| 策略解析失败 | 降级为 `NoneStrategy`（不匹配），打 error 日志 |
| 数据源加载失败 | `ConfigLoadService` 自动降级到下一个 provider |
| 启动时所有数据源都失败 | 向上抛异常，阻断应用启动 |
| 运行期间重载失败 | 吃掉异常，保持旧配置，等待下次重试 |
| 未知的 configType（代码中无对应类） | 解析为 `JSONObject` 存入 `unKnownMap`（供前端使用） |


---

## 7. 监控与可观测性
| 监控项 | 类型 | 说明 |
| --- | --- | --- |
| `ScorpionConfig.Read` | CAT Event | 配置读取事件 |
| `SCORPION_CONFIG_READ_COUNT` | CAT Counter | 读取数据数量，tag: type, action |
| `SCORPION_SWITCHER` | CAT Counter | 切流调用量，tag: switcherId, mode |
| 重载日志 | SLF4J | `parseConfigToMemory` 包含 rawCount, version, unKnownCount 等 |


---

## 8. 约束与注意事项
1. **线程安全**: `MemoryAppConfigs` 内部使用 `ImmutableSortedMap`，引用替换保证读安全
2. **内存共享**: 查询返回的配置对象在内存中共享，**禁止修改字段值**
3. **Java 8 兼容**: 不使用 Java 9+ 特性
4. **唯一性**: `@Config.type()` 全局唯一，重复注册会启动失败
5. **idField 注入**: `JSONDecoder` 会将 configId 注入到 idField 对应字段，自定义 decoder 不会自动注入
