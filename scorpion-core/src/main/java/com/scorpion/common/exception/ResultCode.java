package com.scorpion.common.exception;

/**
 * 统一定义错误借口
 */
public interface ResultCode {
    /**
     * 结果码
     */
    int getCode();

    /**
     * 结果信息
     */
    String getMessage();

    /**
     * 是否可重试
     */
    boolean isRetryable();

    /**
     * 获取code的描述
     */
    default String getCodeDescription() {
        return String.valueOf(getCode());
    }
}
