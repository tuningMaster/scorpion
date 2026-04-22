package com.scorpion.config.impl.strategy;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 组合策略：多条策略 AND 组合
 */
@Data
public class GroupStrategy implements Strategy {

    private final List<Strategy> strategies;

    @Override
    public boolean match(String param) {
        if (StringUtils.isEmpty(param)) {
            return false;
        }
        for (Strategy strategy : strategies) {
            if (strategy.match(param)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建和strategyType同级
     */
    public GroupStrategy(List<Strategy> strategyList) {
        this.strategies = strategyList;
    }
}
