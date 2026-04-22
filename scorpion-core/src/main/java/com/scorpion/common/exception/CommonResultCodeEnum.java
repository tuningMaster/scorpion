package com.scorpion.common.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * SDK 通用错误码枚举
 */
public enum CommonResultCodeEnum implements ResultCode {

    SUCCESS(0, "成功", false),
    SYSTEM_ERROR(10000, "系统异常", true),
    PARAMETER_ILLEGAL(10001, "参数非法", false),
    INTEGRATION_ERROR(10002, "下游异常", true),
    STORE_ERROR(10003, "存储异常", true),
    CONCURRENT_UPDATE(10004, "并发更新", true),
    BILL_DUPLICATE(10005, "流水重复", false),
    CONFIG_ERROR(10006, "配置异常", true),
    CONFIG_NOT_EXIST(10007, "配置不存在", true),
    LOCK_FAIL(10008, "加锁失败", true),
    AUTH_FAIL(10009, "鉴权失败", true);

    private final int code;
    private final String message;
    private final boolean retryable;

    private static final Map<Integer, CommonResultCodeEnum> CODE_ENUM_MAP;

    static {
        Map<Integer, CommonResultCodeEnum> map = new HashMap<>();
        for (CommonResultCodeEnum e : values()) {
            map.put(e.code, e);
        }
        CODE_ENUM_MAP = Collections.unmodifiableMap(map);
    }

    CommonResultCodeEnum(int code, String message, boolean retryable) {
        this.code = code;
        this.message = message;
        this.retryable = retryable;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isRetryable() {
        return retryable;
    }

    @Override
    public String getCodeDescription() {
        return name();
    }

    public static CommonResultCodeEnum findByCode(int code) {
        return CODE_ENUM_MAP.get(code);
    }
}
