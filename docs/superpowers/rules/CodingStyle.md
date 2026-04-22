# 规则：编码风格
> 适用范围：所有模块
>

## 基础约束
| 项目 | 规范 |
| --- | --- |
| Java 版本 | 1.8，不使用更高版本特性 |
| 代码风格 | 使用 Lombok 减少模板代码（`@Data`、`@Builder`、`@Slf4j` 等） |
| 日志 | SLF4J API，禁止 `System.out` |
| JSON | FastJSON 1.2.83 |
| 集合工具 | Guava + Apache Commons Collections |
| 字符串工具 | Apache Commons Lang3 |


## Lombok 使用规范
+ 数据类使用 `@Data`（含 getter/setter/toString/equals/hashCode）
+ 敏感字段或大文本字段使用 `@ToString.Exclude` 排除
+ 不可变类使用 `@Builder` + `@AllArgsConstructor(access = AccessLevel.PRIVATE)`
+ 日志使用 `@Slf4j`，不手写 `LoggerFactory.getLogger()`


