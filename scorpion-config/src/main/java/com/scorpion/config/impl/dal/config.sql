-- 配置表 DDL
-- 唯一键: (app_name, type, custom_id)
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
