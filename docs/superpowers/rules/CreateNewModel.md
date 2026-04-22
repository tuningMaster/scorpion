# 新模块创建
1. 在根 `pom.xml` 的 `<modules>` 中注册新模块
2. 新模块 `pom.xml` 的 parent 指向 `crab-parent`
3. 版本号使用 `${revision}`，不要硬编码
4. 如需自动配置，在 `crab-spring-boot-starter` 中添加对应的 `AutoConfiguration` 类
5. 新模块必须同步在 `crab-test` 中补充测试