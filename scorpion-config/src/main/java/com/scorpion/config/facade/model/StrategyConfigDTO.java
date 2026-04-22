package com.scorpion.config.facade.model;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 策略配置组合对象：策略规则列表 + 该策略下的配置内容
 * <p>strategyList 至少一条</p>
 */
@Data
public class StrategyConfigDTO implements Serializable {

    @NotEmpty
    private List<StrategyDTO> strategyList;

    @ToString.Exclude
    private String content;
}
