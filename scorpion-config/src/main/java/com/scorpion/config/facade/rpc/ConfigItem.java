package com.scorpion.config.facade.rpc;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * RPC层配置项（IDL自动生成，占位实现）
 */
@Data
public class ConfigItem implements Serializable {

    private long id;
    private String createTime;
    private String updateTime;
    private String appName;
    private String type;
    private String customId;
    private String name;
    private String status;
    private String content;
    private String strategyConfigs;
    private String extra;
}
