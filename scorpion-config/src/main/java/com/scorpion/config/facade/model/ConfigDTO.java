package com.scorpion.config.facade.model;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 配置传输对象，Facade层和业务逻辑层使用
 * <p>appName/type/customId 限制：字母数字下划线中划线，最长64</p>
 */
@Data
public class ConfigDTO implements Serializable {

    private long id;
    private Date createTime;
    private Date updateTime;

    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,64}$")
    private String appName;

    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,64}$")
    private String type;

    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,64}$")
    private String customId;

    @Pattern(regexp = "^.{1,256}$")
    private String name;

    @Pattern(regexp = "^(INIT|ONLINE|OFFLINE|DELETE)$")
    private String status;

    @ToString.Exclude
    private String content;

    @ToString.Exclude
    private List<StrategyConfigDTO> strategyConfigs;

    private String extra;
}
