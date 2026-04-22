# 实施计划：scorpion-config 配置读取、解析与内存加载
> 日期: 2026-04-17  
关联需求: [specs/2026-04-17--config-read-parse-load.md](../specs/2026-04-17--config-read-parse-load.md)
>

---

## 实施原则
1. **自底向上**: 先实现基础数据结构，再实现上层编排逻辑
2. **接口先行**: 先定义接口/注解，再实现具体逻辑
3. **每步可测**: 每个 Phase 完成后可独立运行单元测试
4. **向后兼容**: 不破坏 scorpion-core 的现有接口

---

## Phase 1 — 数据模型与注解定义
**预计工时**: 2 天

本阶段为整个配置管理功能的地基，需要完成数据库表设计、三层数据模型、枚举体系和模型转换工具。

### Step 1.1 — 数据库表 DDL + 注解定义
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 1.1.1 | 设计配置表 DDL（字段、类型、约束、唯一键） | `config/impl/dal/config.sql` | ✅ |
| 1.1.2 | 定义 `@Config` 注解（type, idField, decoder, indices） | `config/annotation/Config.java` | ✅ |
| 1.1.3 | 定义 `@ConfigIndex` 注解（type, key） | `config/annotation/ConfigIndex.java` | ✅ |


**关键决策**:

+ 表唯一键为 `(app_name, type, custom_id)`
+ `content` 和 `strategy_configs` 使用 TEXT 类型（兼容 MySQL 5.6）
+ `@Config` 需标注 `@CompileScanMeta` 以支持编译时扫描

### Step 1.2 — 枚举体系
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 1.2.1 | 实现 `ConfigStatusEnum`（INIT/ONLINE/OFFLINE/DELETE） | `config/facade/enums/ConfigStatusEnum.java` | ✅ |
| 1.2.2 | 实现 `StrategyTypeEnum`（WHITE_LIST/PERCENT/BETA）+ `findByName()` | `config/facade/enums/StrategyTypeEnum.java` | ✅ |
| 1.2.3 | 实现 `ConfigChangeTypeEnum`（WHITE_LIST/FREEDOM/ROLLBACK） | `config/facade/enums/ConfigChangeTypeEnum.java` | ✅ |
| 1.2.4 | 实现 `OperateTypeEnum`（CREATE/UPDATE） | `config/facade/enums/OperateTypeEnum.java` | ✅ |


**规范**:

+ 所有枚举提供 `findByName(String)` 静态查找方法
+ 内部使用 `Collections.unmodifiableMap` 缓存 name→enum 映射

### Step 1.3 — DAL 层数据对象
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 1.3.1 | 实现 `ConfigDO`（与数据库表一一映射） | `config/impl/dal/ConfigDO.java` | ✅ |


**注意事项**:

+ `id` 用 `Long`（包装类型），新增时为 null
+ `strategyConfigs` 保持为 `String`（原始 JSON），不在 DO 层解析
+ 实现 `Serializable`

### Step 1.4 — Facade 层传输对象
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 1.4.1 | 实现 `StrategyDTO`（策略规则，字段平铺） | `config/facade/model/StrategyDTO.java` | ✅ |
| 1.4.2 | 实现 `StrategyConfigDTO`（策略列表 + 配置内容） | `config/facade/model/StrategyConfigDTO.java` | ✅ |
| 1.4.3 | 实现 `ConfigDTO`（配置传输对象） | `config/facade/model/ConfigDTO.java` | ✅ |
| 1.4.4 | 实现 `AppAllConfigDTO`（全量配置包装） | `config/facade/model/AppAllConfigDTO.java` | ✅ |


**关键决策**:

+ `ConfigDTO.id` 使用 `long`（基本类型）— 避免与 IDL ConfigItem 转换时 NPE
+ `ConfigDTO.content` 和 `strategyConfigs` 标注 `@ToString.Exclude` — 防止大文本污染日志
+ `ConfigDTO` 的 `appName`/`type`/`customId` 使用 `@Pattern` 校验（字母数字下划线中划线，最长 64）
+ `StrategyDTO` 采用字段平铺设计（非继承），不同策略类型用不同字段
+ `StrategyConfigDTO.strategyList` 标注 `@NotEmpty`
+ `AppAllConfigDTO.version` 标注 `@Min(0)`

### Step 1.5 — 模型转换工具
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 1.5.1 | 实现 `ConfigConverter`（DO↔DTO↔RPC 全部转换方法） | `config/facade/convert/ConfigConverter.java` | ✅ |


**需实现的转换方法（6 对单条 + 6 对批量）**:

| 方向 | 方法名 | 核心逻辑 |
| --- | --- | --- |
| DTO → DO | `dto2Do()` | `strategyConfigs` List 序列化为 JSON String |
| DO → DTO | `do2Dto()` | `strategyConfigs` JSON String 反序列化为 List（FastJSON TypeReference） |
| DO → RPC | `do2RpcModel()` | Date 格式化为 String（DateUtils.formatDate） |
| RPC → DO | `rpcModel2Do()` | String 解析为 Date（DateUtils.parseDate） |
| DTO → RPC | `dto2RpcModel()` | strategyConfigs List 序列化 + Date 格式化 |
| RPC → DTO | `rpcModel2Dto()` | strategyConfigs 反序列化 + Date 解析 |


**空值安全规范**: 入参 null 返回 null，入参空 List 返回空 List。

### Step 1.6 — 单元测试
| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 1.6.1 | `ConfigConverter` 转换测试（DO↔DTO 正向/反向） | `test/config/convert/ConfigConverterTest.java` | ✅ |
| 1.6.2 | `ConfigConverter` 空值边界测试 | `test/config/convert/ConfigConverterTest.java` | ✅ |
| 1.6.3 | `StrategyDTO` JSON 序列化/反序列化测试 | `test/config/model/StrategyDTOTest.java` | ✅ |
| 1.6.4 | `ConfigDTO` 的 strategyConfigs 完整转换链路测试 | `test/config/model/ConfigDTOTest.java` | ✅ |
| 1.6.5 | 枚举 `findByName()` 正常/异常路径测试 | `test/config/enums/EnumTest.java` | ✅ |


**验证点**:

+ 模型类可正常编译，Lombok 生成的 getter/setter/toString 正确
+ `ConfigConverter.do2Dto()` 能正确将 `strategyConfigs` JSON 字符串反序列化为 `List<StrategyConfigDTO>`
+ `ConfigConverter.dto2Do()` 能正确将 `List<StrategyConfigDTO>` 序列化为 JSON 字符串
+ RPC 方向的 Date↔String 转换正确
+ 所有转换方法的 null 入参返回 null，空 List 返回空 List
+ `StrategyDTO` 的三种策略类型 JSON 序列化结果正确
+ 枚举 `findByName()` 对有效 name 返回对应枚举，对无效 name 返回 null

---

## Phase 2 — 配置解码器
**预计工时**: 0.5 天

| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 2.1 | 定义 `ConfigDecoder<T>` 接口 | `config/api/decoder/ConfigDecoder.java` | ✅ |
| 2.2 | 实现 `JSONDecoder<T>` | `config/api/decoder/JSONDecoder.java` | ✅ |
| 2.3 | 编写 `JSONDecoder` 单元测试 | `test/config/decoder/JSONDecoderTest.java` | ✅ |


**验证点**:

+ JSON 字符串能正确解析为目标对象
+ `idField` 能正确注入 id 值
+ null/空/非法 JSON 的容错处理

---

## Phase 3 — 灰度策略
**预计工时**: 1 天

| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 3.1 | 定义 `Strategy` 接口 + `parse()` 工厂方法 | `config/impl/strategy/Strategy.java` | ✅ |
| 3.2 | 实现 `WhiteListStrategy` | `config/impl/strategy/WhiteListStrategy.java` | ✅ |
| 3.3 | 实现 `PercentStrategy` | `config/impl/strategy/PercentStrategy.java` | ✅ |
| 3.4 | 实现 `BetaStrategy` | `config/impl/strategy/BetaStrategy.java` | ✅ |
| 3.5 | 实现 `DefaultStrategy` | `config/impl/strategy/DefaultStrategy.java` | ✅ |
| 3.6 | 实现 `NoneStrategy` | `config/impl/strategy/NoneStrategy.java` | ✅ |
| 3.7 | 实现 `GroupStrategy` | `config/impl/strategy/GroupStrategy.java` | ✅ |
| 3.8 | 编写策略单元测试 | `test/config/strategy/StrategyTest.java` | ✅ |


**验证点**:

+ 白名单：精确匹配、多值、空值边界
+ 百分比：短 key repeat、边界值 0/100、分布均匀性
+ 组合策略：多条件 AND 逻辑
+ `Strategy.parse()`：单条/多条/空列表/null

---

## Phase 4 — 配置描述与索引
**预计工时**: 1 天

| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 4.1 | 实现 `ConfigIndexDescriptor` | `config/impl/descriptor/ConfigIndexDescriptor.java` | ✅ |
| 4.2 | 实现 `ConfigDescriptor` | `config/impl/descriptor/ConfigDescriptor.java` | ✅ |
| 4.4 | 实现 `ConfigDescriptors` 注册表 | `config/impl/descriptor/ConfigDescriptors.java` | ✅ |
| 4.5 | 编写描述器单元测试 | `test/config/descriptor/ConfigDescriptorTest.java` | ⬜ |


**验证点**:

+ `@Config` 注解解析正确
+ `toMemory()` 转换正确（含策略/默认配置）
+ type 重复检测
+ 索引构建和 indexKey 提取

---

## Phase 5 — 内存数据结构
**预计工时**: 1.5 天

| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 5.1 | 实现 `MemoryConfigIndex`（含 Comparable） | `config/impl/memory/MemoryConfigIndex.java` | ✅ |
| 5.2 | 实现 `MemoryStrategyConfig` | `config/impl/memory/MemoryStrategyConfig.java` | ✅ |
| 5.3 | 实现 `MemoryConfig` | `config/impl/memory/MemoryConfig.java` | ✅ |
| 5.4 | 实现 `MemoryConfigSelector` | `config/impl/memory/MemoryConfigSelector.java` | ✅ |
| 5.5 | 实现 `MemoryAppConfigs` | `config/impl/memory/MemoryAppConfigs.java` | ✅ |
| 5.6 | 编写 `MemoryConfigIndex` 排序测试 | `test/config/memory/MemoryConfigIndexTest.java` | ✅ |
| 5.7 | 编写 `MemoryAppConfigs` 查询测试 | `test/config/memory/MemoryAppConfigsTest.java` | ⬜ |


**验证点**:

+ `MemoryConfigIndex` 排序正确性（null 处理、前缀匹配范围）
+ `findById` / `findByIds` / `findByType` / `findByIndex` 各查询正确
+ 策略选择器在索引查询场景下的过滤逻辑
+ `unKnownMap` 正确接纳未注册类型
+ 去重逻辑（索引查询时同一 configId 不重复返回）

---

## Phase 6 — 数据源加载
**预计工时**: 1 天

| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 6.1 | 定义 `ConfigProvider` 接口 | `config/api/provider/ConfigProvider.java` | ✅ |
| 6.2 | 定义 `ConfigRepository` DAO 接口 | `config/impl/dal/ConfigRepository.java` | ✅ |
| 6.3 | 实现 `NamedParameterJdbcTemplateConfigRepository` | `config/impl/loader/JdbcTemplateConfigRepository.java` | ✅ |
| 6.4 | 实现 `DatabaseConfigProvider` | `config/impl/loader/DatabaseConfigProvider.java` | ✅ |
| 6.5 | 实现 `ConfigLoadService` | `config/impl/loader/ConfigLoadService.java` | ✅ |
| 6.6 | 编写 `ConfigLoadService` 单元测试 | `test/config/loader/ConfigLoadServiceTest.java` | ✅ |


**验证点**:

+ 多 provider 按 order 排序
+ 主源成功直接返回 + 执行 backup
+ 主源失败自动降级到备份源
+ 全部失败返回 null

---

## Phase 7 — 内存服务与引擎编排
**预计工时**: 1 天

| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 7.1 | 实现 `MemoryConfigService` | `config/impl/memory/MemoryConfigService.java` | ✅ |
| 7.2 | 实现 `ConfigHolder` 静态 API | `config/api/holder/ConfigHolder.java` | ✅ |
| 7.3 | 实现 `ConfigServiceHolder` 上下文 | `config/core/ConfigServiceHolder.java` | ✅ |
| 7.4 | 实现 `ConfigEngine` 引擎 | `config/core/ConfigEngine.java` | ✅ |
| 7.5 | 编写 `MemoryConfigService` 集成测试 | `test/config/memory/MemoryConfigServiceTest.java` | ⬜ |


**验证点**:

+ 初始化时立即加载配置
+ 定时重载机制工作正常
+ 启动加载失败时抛异常阻断
+ 运行期加载失败时保持旧数据
+ `ConfigHolder` 各 API 委托正确
+ `shutdown()` 正确关闭线程池

---

## Phase 8 — 配置变更回调
**预计工时**: 0.5 天

| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 8.1 | 定义 `ConfigChangeCallback<C>` 接口 | `config/api/callback/ConfigChangeCallback.java` | ✅ |
| 8.2 | 实现 `ConfigCallbackFactory` | `config/api/callback/ConfigCallbackFactory.java` | ✅ |
| 8.3 | 编写回调测试 | `test/config/callback/ConfigChangeCallbackTest.java` | ⬜ |


**验证点**:

+ 泛型反射正确获取配置类型
+ `decodeDefaultConfig` / `decodeStrategyConfigs` 正确解析
+ 回调抛 `CommonException` 时正确传播

---

## Phase 9 — 切流工具
**预计工时**: 0.5 天

| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 9.1 | 实现 `SwitcherConfig` 配置类 | `config/api/switcher/SwitcherConfig.java` | ✅ |
| 9.2 | 实现 `SwitcherModeEnum` | `config/api/switcher/SwitcherModeEnum.java` | ✅ |
| 9.3 | 定义 `CheckPolicy` 接口 | `config/api/switcher/policy/CheckPolicy.java` | ✅ |
| 9.4 | 实现 `DefaultCheckPolicy` | `config/api/switcher/policy/DefaultCheckPolicy.java` | ✅ |
| 9.5 | 实现 `Switchers` 工具类 | `config/api/switcher/Switchers.java` | ✅ |
| 9.6 | 编写 `Switchers` 单元测试 | `test/config/switcher/SwitchersTest.java` | ⬜ |


**验证点**:

+ config == null → 执行 old
+ OLD / NEW / BOTH 三种模式
+ BOTH 模式下 policy 决策逻辑
+ `hit()` / `miss()` / `callRun()` 简便方法

---

## Phase 10 — Spring Boot 自动配置
**预计工时**: 0.5 天

| # | 任务 | 文件路径 | 状态 |
| --- | --- | --- | --- |
| 10.1 | 实现 `ScorpionConfigProperties` | `autoconfigure/properties/ScorpionConfigProperties.java` | ✅ |
| 10.2 | 实现 `ScorpionConfigAutoConfiguration` | `autoconfigure/ScorpionConfigAutoConfiguration.java` | ✅ |
| 10.3 | 注册 `spring.factories` | `META-INF/spring.factories` | ✅ |


**验证点**:

+ `scorpion.config.enabled=false` 时不初始化
+ 必填属性缺失时启动报错提示清晰
+ Bean 创建顺序正确

---

## 依赖关系图
```plain
Phase 1 (数据模型)
  ↓
Phase 2 (解码器)  ←──────────────────────────────┐
  ↓                                               │
Phase 3 (灰度策略)                                 │
  ↓                                               │
Phase 4 (配置描述) ──依赖──▶ Phase 2, Phase 3      │
  ↓                                               │
Phase 5 (内存数据结构) ──依赖──▶ Phase 3, Phase 4   │
  ↓                                               │
Phase 6 (数据源加载) ──依赖──▶ Phase 1              │
  ↓                                               │
Phase 7 (服务与引擎) ──依赖──▶ Phase 4, 5, 6       │
  ↓                                               │
Phase 8 (变更回调) ──依赖──▶ Phase 4               │
  ↓                                               │
Phase 9 (切流工具) ──依赖──▶ Phase 7               │
  ↓                                               │
Phase 10 (自动配置) ──依赖──▶ All above            │
```

---

## 风险与注意事项
1. **编译时处理器依赖**: Phase 4 依赖 scorpion-core 的 `CompileScanMetaAnnotationProcessor`，需确保 scorpion-core 先构建完成
2. **CAT 依赖**: `MemoryAppConfigs` 和 `Switchers` 中使用了 CAT 监控，CAT 为 provided scope，测试时需 Mock
3. **SortedMap 性能**: `MemoryConfigIndex` 的 `compareTo` 实现是热路径，需手写避免装箱开销
4. **内存占用**: 全量配置加载到内存，需关注大数据量场景的内存压力
5. **线程池关闭**: `shutdown()` 后需确保不再接受新的 reload 任务
