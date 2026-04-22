package com.scorpion.config.impl.memory;

import com.scorpion.config.impl.strategy.Strategy;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

/**
 * 策略配置项：灰度策略 + 配置内容 + 索引集合
 */
@Value
@Builder
public class MemoryStrategyConfig<T> {

    /**
     * 策略
     */
    Strategy strategy;

    /**
     * 配置内容
     */
    T config;

    /**
     * 配置索引（不含id索引，只有自定义索引）
     */
    Set<MemoryConfigIndex> indices;
}
