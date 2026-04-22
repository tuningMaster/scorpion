package com.scorpion.config.facade.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 策略类型枚举
 */
public enum StrategyTypeEnum {
    // 白名单策略
    WHITE_LIST,
    // 百分比策略
    PERCENT,
    // BETA全量生效
    BETA;

    private static final Map<String, StrategyTypeEnum> NAME_ENUM_MAP = Collections.unmodifiableMap(
            Arrays.stream(values()).collect(Collectors.toMap(
                    StrategyTypeEnum::name,
                    Function.identity()
            ))
    );

    public static StrategyTypeEnum findByName(String name) {
        return NAME_ENUM_MAP.get(name);
    }
}
