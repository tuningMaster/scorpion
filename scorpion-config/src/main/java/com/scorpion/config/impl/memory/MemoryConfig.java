package com.scorpion.config.impl.memory;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * 单条配置的内存表示
 */
@Value
@Builder
public class MemoryConfig<T> {

    /**
     * 配置类型
     */
    String type;

    /**
     * 配置id
     */
    String id;

    /**
     * 配置项
     */
    List<MemoryStrategyConfig<T>> strategyConfigs;

    /**
     * 配置选择器
     */
    MemoryConfigSelector<T> configSelector;
}
