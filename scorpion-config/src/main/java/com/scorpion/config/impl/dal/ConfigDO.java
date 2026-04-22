package com.scorpion.config.impl.dal;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 配置表数据对象，与数据库表一一映射
 */
@Data
public class ConfigDO implements Serializable {

    private Long id;
    private String appName;
    private String type;
    private String customId;
    private String name;
    private String status;
    private String content;
    private String strategyConfigs;
    private String extra;
    private Date createTime;
    private Date updateTime;
}
