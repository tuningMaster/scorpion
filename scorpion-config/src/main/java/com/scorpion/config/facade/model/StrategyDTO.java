package com.scorpion.config.facade.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 单条策略规则，字段平铺设计（非继承）
 */
@Data
public class StrategyDTO implements Serializable {

    @NotNull
    private String strategyType;

    private String whiteList;

    private Integer percent;
}
