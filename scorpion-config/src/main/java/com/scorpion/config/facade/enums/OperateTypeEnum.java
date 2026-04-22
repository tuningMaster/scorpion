package com.scorpion.config.facade.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 操作类型枚举
 */
public enum OperateTypeEnum {
    // 新建配置
    CREATE,
    // 修改配置
    UPDATE;

    private static final Map<String, OperateTypeEnum> NAME_ENUM_MAP = Collections.unmodifiableMap(
            Arrays.stream(values()).collect(Collectors.toMap(
                    OperateTypeEnum::name,
                    Function.identity()
            ))
    );
    public static OperateTypeEnum findByName(String name) {
        return NAME_ENUM_MAP.get(name);
    }
}
