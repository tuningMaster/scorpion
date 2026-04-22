# CLAUDE.md — scorpion SDK 项目知识库

> 本文件是 AI 辅助开发的核心知识沉淀，包含项目约定、规则和架构信息。

---

## 项目概述

scorpion 是Java SDK 工具库，为业务应用提供**配置管理**、**RPC 增强**、**分布式锁**、**调试工具**、**消息通知**等通用能力。

- **GroupId**: `org.springframework`
- **当前版本**: `0.0.1`（通过 `${revision}` 统一管理）
- **Java 版本**: 1.11
- **构建工具**: Maven 多模块
- **Spring**: 5.1.8.RELEASE

---

## 模块分层规范

项目采用分层架构，模块间依赖遵循**自底向上、单向依赖**原则：

```
┌─────────────────────────────────────────────────────────────────┐
│  Layer 4: Spring Boot Starter（自动配置层）                       │
│  ┌───────────────────────────────┐  ┌────────────────────────────┐  │
│  │ scorpion-spring-boot-starter  │  │ scorpion-debug-spring-boot-│  │
│  │ (聚合所有模块的自动配置)          │  │ starter (Debug 独立 Starter)│  │
│  └───────────────────────────────┘  └────────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│  Layer 3: 功能模块层（各自独立，仅依赖 scorpion-core）                  │
│  ┌──────────────────┐ ┌──────────────────┐ ┌───────────────┐ ┌───────────────┐ │
│  │  scorpion-config │ │  scorpion-spring │ │ scorpion-utils│ │ scorpion-debug│ │
│  │  配置管理引擎      │ │  Spring 增强      │ │ 通用工具       │ │  调试工具      │ │
│  └──────────────────┘ └──────────────────┘ └───────────────┘ └───────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│  Layer 2: 核心基础层                                             │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  scorpion-core                                           │   │
│  │  通用模型、异常体系、RPC 模板、编译时处理器                    │   │
│  └──────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────┤
│  Layer 1: 测试层（依赖所有模块）                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  scorpion-test                                           │   │
│  │  测试基类、Mock 配置、集成测试                               │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 依赖关系图

```
scorpion-core (无内部依赖)
  ↑
  ├── scorpion-config
  ├── scorpion-spring
  ├── scorpion-utils
  └── scorpion-debug
        ↑
        └── scorpion-debug-spring-boot-starter
  ↑ ↑ ↑ ↑
  scorpion-spring-boot-starter (聚合所有核心模块)
  scorpion-test (测试依赖所有模块)
```

### 分层规则

1. **Layer 2 (scorpion-core)** 不得依赖任何上层模块
2. **Layer 3** 各功能模块之间**不得相互依赖**，只能依赖 scorpion-core
3. **Layer 4 (Starter)** 负责聚合和自动配置，不含业务逻辑
4. 新功能模块应在 Layer 3 新建，遵循"仅依赖 scorpion-core"原则

---

## 模块详细说明

### scorpion-core — 核心基础模块

通用模型、异常体系、RPC 模板。非 Spring 项目也可使用。

| 包 | 职责 |
|---|---|
| `common.exception` | 统一异常体系：`CommonException`、`CommonResultCodeEnum`、`AssertUtils` |
| `common.rpc` | RPC 模板方法：`FacadeTemplate`、`FacadeCallback`、`RpcExecutor` |
| `common.constants` | 通用常量 |
| `common.utils` | 工具类：`AopUtils`、`DateUtils`、`EnvUtils` |
| `core.annotation.scan` | 编译时扫描元注解：`@CompileScanMeta` |
| `core.processor` | 注解处理器：`CompileScanMetaAnnotationProcessor` |

**错误码规范**:
- `0` — SUCCESS
- `10000` — SYSTEM_ERROR（系统异常兜底）
- `10001` — PARAMETER_ILLEGAL
- `10002` — LOCK_FAIL
- `10003` — IDEMPOTENT

### scorpion-config — 配置管理模块

内存配置服务，支持灰度策略、索引查询、定时重载。

| 包 | 职责 |
|---|---|
| `annotation` | `@Config`、`@ConfigIndex` 注解定义 |
| `api.holder` | `ConfigHolder` — 配置访问静态入口 |
| `api.switcher` | `Switchers` — 切流工具（OLD/NEW/BOTH 模式） |
| `api.decoder` | `ConfigDecoder` 接口、`JSONDecoder` 实现 |
| `api.provider` | `ConfigProvider` 接口、`DatabaseConfigProvider` |
| `api.callback` | `ConfigChangeCallback` — 配置变更回调 |
| `impl.memory` | 内存配置服务、配置索引、配置选择器 |
| `impl.strategy` | 灰度策略：白名单、百分比、BETA、Group |
| `impl.dal` | 数据访问层：`ConfigRepository`、`ConfigDO` |
| `facade` | 配置管理 Facade、模型转换 |

### scorpion-spring — Spring 增强模块

基于 Spring 的通用组件，包含 AOP 切面、分布式锁。

| 包 | 职责 |
|---|---|
| `server` | `@CrabRpc` 注解 + `CrabRpcAspect` — RPC 统一异常处理和日志 |
| `lock` | `@RedisLock` 注解 + `RedisLockAspect` — 分布式锁（基于 Jedis） |
| `client` | `CrabClientProxyPostProcessor` — Client 代理日志 |
| `mybatis` | `CrabSqlLogInterceptor` — MyBatis SQL 日志拦截器 |
| `utils` | `SpELUtils`、`LogUtils` |

**切面执行顺序**（`AspectOrderConstants`）按常量定义排列。

### crab-utils — 通用工具模块

消息通知和监控集成。

| 包 | 职责 |
|---|---|
| `hi` | 钉钉消息发送：`HiMessageSendUtil`（Text/Markdown/Card） |
| `wx` | 企业微信消息发送：`WxMessageSendUtil`（Text/Markdown/Image/News） |
| `log.appender` | `ScorpionCatLogbackAppender` — CAT 监控 Logback Appender |

### scorpion-debug — 调试工具模块

在线调试、Groovy 脚本执行、Bean 反射。

| 包 | 职责 |
|---|---|
| `facade` | `ScorpionDebugServerFacade` — 列出 Bean、方法、调用方法 |
| `script` | `GroovyUtils`（LRU 缓存）、`GroovySpringFactory`（支持 DI 注入） |
| `utils` | `ScorpionDebugUtils`、`DefaultValueUtils` |

### scorpion-spring-boot-starter — 自动配置模块

通过 `META-INF/spring.factories` 注册三个自动配置类：

- `ScorpionConfigAutoConfiguration` — 配置模块（`scorpion.config.*`）
- `ScorpionSpringAutoConfiguration` — Spring 模块（`scorpion.spring.*`）
- `ScorpionDebugAutoConfiguration` — Debug 模块（`scorpion.debug.*`）

---

## 核心注解速查

| 注解 | 模块 | 用途 | 关键属性 |
|------|------|------|---------|
| `@Config` | scorpion-config | 标记配置类 | `type`, `idField`, `decoder`, `indices` |
| `@ConfigIndex` | scorpion-config | 定义配置索引 | `indexField`, `field` |
| `@Scorpion` | scorpion-spring | RPC 统一异常处理 | `enable`, `logStrategy`, `printContext`, `defaultErrorMsg` |
| `@RedisLock` | scorpion-spring | 分布式锁 | `group`, `value(SpEL)`, `lockTime`, `lockFailedMsg` |
| `@CompileScanMeta` | scorpion-core | 编译时扫描元注解 | `resourceFile` |

---

## 构建与开发

### 常用命令

```bash
# 全量构建
mvn clean install

# 跳过测试构建
mvn clean install -DskipTests

# 仅运行测试
mvn test

# 单模块构建
mvn clean install -pl scorpion-config -am
```

### 版本管理

版本通过根 `pom.xml` 的 `<revision>` 属性统一管理，使用 `flatten-maven-plugin` 在打包时替换。修改版本只需改一处：

```xml
<properties>
    <revision>0.0.1</revision>
</properties>
```

---

## 编码规则
> **所有代码生成/修改必须遵循[`docs/superpowers/rules/`](docs/superpowers/rules)目录下规则文件**

| 规则文件                                                                  | 内容                   |
|-----------------------------------------------------------------------|----------------------|
| [`SingletonPattern.md`](docs/superpowers/rules/SingletonPattern.md)   | 单例必须用Holder模式，禁止用DCL |
| [`ConcurrencyRules.md`](docs/superpowers/rules/ConcurrencyRules.md)   | 不可变集合、定时任务、线程池规范     |
| [`ExceptionHandling.md`](docs/superpowers/rules/ExceptionHandling.md) | 统一异常体系，错误码规范         |
| [`CodingStyle.md`](docs/superpowers/rules/CodingStyle.md)             | Java版本、Lombok、日志     |
| [`CreateNewModel.md`](docs/superpowers/rules/CreateNewModel.md)       | 新模块创建                |
| [`UnitTesting.md`](docs/superpowers/rules/UnitTesting.md)             | 测试命名、分层、Mock、覆盖率目标   |




---

## 测试规范

### 现有测试结构

测试代码集中在 `scorpion-test` 模块：

```
scorpion-test/src/test/java/
├── AbstractSpringTest.java          # Spring 环境测试基类
├── config/
│   ├── mock/                        # 配置模块测试 + Mock
│   └── scan/                        # 编译时扫描测试
├── debug/
│   ├── mock/                        # Debug Mock DAO
│   ├── script/                      # Groovy 脚本测试
│   └── utils/                       # DefaultValueUtils 测试
└── utils/
    └── SpELUtilsTest.java           # SpEL 工具测试
```

### 测试框架

- **JUnit 4** (4.13.2) — 主要测试框架
- **TestNG** (6.9.4) — 辅助测试
- **Mockito** (2.28.2) — Mock 框架
- **PowerMock** (2.0.9) — 静态方法 Mock
- **Spring Boot Test** — Spring 环境集成测试

### 测试覆盖目标（SDK 项目要求高覆盖率）

作为公共 SDK，测试覆盖率直接影响下游业务的稳定性。各模块覆盖目标：

| 模块 | 当前覆盖情况 | 目标覆盖率 | 优先级 |
|------|------------|-----------|-------|
| scorpion-core | 异常体系、RPC 模板缺少测试 | ≥ 80% | P0 |
| scorpion-config | 有基础配置测试 | ≥ 85% | P0 |
| scorpion-spring | SpEL 工具有测试，切面缺少测试 | ≥ 80% | P0 |
| scorpion-utils | 无测试 | ≥ 70% | P1 |
| scorpion-debug | Groovy/DefaultValueUtils 有测试 | ≥ 75% | P1 |

### 测试编写规范

1. **测试基类**: Spring 环境测试必须继承 `AbstractSpringTest`
2. **命名规范**: 测试类 `XxxTest.java`，测试方法 `test_方法名_场景描述()`
3. **Mock 数据**: 放在对应模块的 `mock/` 子目录下
4. **测试分层**:
    - **单元测试**: 纯逻辑测试，不依赖 Spring 上下文
    - **集成测试**: 需要 Spring 环境，继承 `AbstractSpringTest`
5. **断言**: 优先使用 JUnit Assert，复杂断言可使用 AssertJ

### 待补充的测试（优先级从高到低）

#### P0 — 核心模块
- [ ] `CommonException` 异常构造和错误码
- [ ] `AssertUtils` 所有断言方法
- [ ] `FacadeTemplate` RPC 模板（正常/异常路径）
- [ ] `ConfigHolder` 配置查询（findById/findAll/findByIndex）
- [ ] `MemoryConfigService` 内存配置加载和重载
- [ ] `ConfigDescriptor` / `ConfigDescriptors` 配置描述解析
- [ ] `Strategy` 各灰度策略（白名单/百分比/BETA）
- [ ] `Switchers` 切流工具
- [ ] `ScorpionRpcAspect` RPC 切面逻辑
- [ ] `RedisLockAspect` 分布式锁切面

#### P1 — 工具模块
- [ ] `HiMessageSendUtil` 钉钉消息发送
- [ ] `WxMessageSendUtil` 企业微信消息发送
- [ ] `ScorpionDebugServerFacade` Debug 服务
- [ ] `ConfigConverter` 模型转换

---

## 编码规范

### 通用规则

- **Java 版本**: 1.8，不使用更高版本特性
- **代码风格**: 使用 Lombok 减少模板代码（`@Data`、`@Builder`、`@Slf4j` 等）
- **日志**: SLF4J API，禁止直接使用 `System.out`
- **JSON 序列化**: FastJSON 1.2.83
- **集合工具**: Guava + Apache Commons Collections
- **字符串工具**: Apache Commons Lang3

### 异常处理规范

- 业务异常统一使用 `CommonException` 或其子类
- 错误码参考 `CommonResultCodeEnum`，新增错误码需在枚举中统一定义
- 对外接口必须捕获所有异常并转换为 `CommonException`

### 新模块创建规范

1. 在根 `pom.xml` 的 `<modules>` 中注册新模块
2. 新模块 `pom.xml` 的 parent 指向 `scorpion-parent`
3. 版本号使用 `${revision}`，不要硬编码
4. 如需自动配置，在 `scorpion-spring-boot-starter` 中添加对应的 `AutoConfiguration` 类
5. 新模块必须同步在 `scorpion-test` 中补充测试

---

## 设计模式速查

| 模式 | 应用位置 | 说明 |
|------|---------|------|
| Template Method | `FacadeTemplate` | RPC 公共逻辑处理模板 |
| Strategy | `Strategy` + 各实现 | 灰度策略（白名单/百分比/BETA） |
| Factory | `ConfigCallbackFactory` | 配置回调工厂 |
| Decorator/Proxy | `ScorpionClientProxyPostProcessor` | Client 代理日志装饰 |
| AOP/Aspect | `ScorpionRpcAspect`、`RedisLockAspect` | 横切关注点处理 |
| Singleton/Holder | `ConfigHolder` | 配置静态访问入口 |

---

## 关键配置属性

```yaml
scorpion:
  facade-port: 9090              # SPI Facade 统一端口（必需）

  config:
    enabled: true                 # 配置模块开关
    app-name: your-app            # 应用名（必需）
    table-name: your_config_table # 配置表名（必需）
    reload-interval: 30000        # 重载间隔（毫秒）
    thread-size: 5                # Facade 线程数

  spring:
    log:
      enabled: true
    redis-lock:
      enabled: true
      app-name: your-app
      jedis-bean-name: jedis

  debug:
    enabled: false
    thread-size: 1

  mybatis:
    log:
      enable: true
      print:
        sql: false
```
