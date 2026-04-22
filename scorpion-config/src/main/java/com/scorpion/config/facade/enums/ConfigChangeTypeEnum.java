package com.scorpion.config.facade.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 配置变更类型枚举
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ConfigChangeTypeEnum {
    WHITE_LIST("白名单变更"),
    FREEDOM("自由变更"),
    ROLLBACK("回滚");

    private final String description;

    private static final Map<String, ConfigChangeTypeEnum> NAME_ENUM_MAP = Collections.unmodifiableMap(
            Arrays.stream(values()).collect(Collectors.toMap(
                    ConfigChangeTypeEnum::name,
                    Function.identity()
            )));

    public static ConfigChangeTypeEnum findByName(String name) {
        return NAME_ENUM_MAP.get(name);
    }
}
