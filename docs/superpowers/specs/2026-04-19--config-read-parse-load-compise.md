# read 细节修改设计：scorpion-config 配置读取、解析与内存加载
> 日期: 2026-04-17  
状态: 待开发  
模块: scorpion-config  
负责人: tuningMaster
>

---
- 已经在ConfigServiceHolder中新增了变量，需要将这部分变量在ConfigEngine的init中进行初始化
- ConfigCallbackFactory需要实现ApplicationContextAware；
  - private final List<ConfigChangeCallback<?>> callbackList; 这个需要修改成Map<String,ConfigChangeCallback<?>> callbackMap;
  - public ConfigChangeCallback<?> getConfigCallback(String type) // 获取配置回调器
  - public ConfigDTO doCallback(ConfigChangeTypeEnum changeType, OperateTypeEnum operateType, ConfigDTO config); // 配置回调
  
- ConfigChangeCallback
  - 配置校验，业务抛出{CommonException}会回显到配置平台的前端页面
  - 配置内容修改，例如查询某下游or存储填充配置
  - 出发启动线程脚本等其他操作
