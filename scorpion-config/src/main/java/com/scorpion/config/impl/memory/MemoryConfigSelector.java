package com.scorpion.config.impl.memory;

import com.scorpion.config.impl.strategy.Strategy;
import org.apache.commons.lang3.Range;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * 配置选择器，根据灰度参数从策略配置中选择匹配的配置
 */
@FunctionalInterface
public interface MemoryConfigSelector<T> {
    /**
     * 选择目标配置
     */
    T select(String param, Range<MemoryConfigIndex> filterIndexRange);

    static <T> MemoryConfigSelector<T> build(List<MemoryStrategyConfig<T>> strategyConfigs) {
        return (param, filterIndexRange) -> {
            // 循环匹配，获取结果
            for (MemoryStrategyConfig<T> strategyConfig : strategyConfigs) {
                // 优先匹配策略
                if (strategyConfig.getStrategy().match(param)) {
                    if (filterIndexRange == null) {
                        return strategyConfig.getConfig();
                    }

                    // 如果是按照索引查询的，需要按索引过滤，但是如果第一份配置索引就没有命中，也不会匹配下一个配置，直接返回
                    if (strategyConfig.getIndices().stream().anyMatch(filterIndexRange::contains)) {
                        return strategyConfig.getConfig();
                    } else {
                        return null;
                    }
                }
            }

            return null;
        };
    }
}
