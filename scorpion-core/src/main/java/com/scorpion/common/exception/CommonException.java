package com.scorpion.common.exception;

import lombok.Getter;

/**
 * 通用异常基类
 */
@Getter
public class CommonException extends RuntimeException {

    /**
     * 结果码
     */
    private final int code;

    /**
     * 结果信息
     */
    private final String message;

    /**
     * 是否可重试
     */
    private final boolean retryable;

    /**
     * 错误码描述
     */
    private final String codeDescription;

    public CommonException(ResultCode resultCode) {
        this(resultCode, resultCode.getMessage(), resultCode.getCodeDescription(), null);
    }

    public CommonException(String message) {
        this(CommonResultCodeEnum.SYSTEM_ERROR, message, CommonResultCodeEnum.SYSTEM_ERROR.getCodeDescription(), null);
    }

    public CommonException(int code, String message) {
        this(code, message, CommonResultCodeEnum.SYSTEM_ERROR.isRetryable(), CommonResultCodeEnum.SYSTEM_ERROR.getCodeDescription(), null);
    }

    public CommonException(String message, Throwable cause) {
        this(CommonResultCodeEnum.SYSTEM_ERROR, message, CommonResultCodeEnum.SYSTEM_ERROR.getCodeDescription(), cause);
    }

    public CommonException(ResultCode resultCode, String message) {
        this(resultCode, message, resultCode.getCodeDescription(), null);
    }

    public CommonException(ResultCode resultCode, String message, Throwable cause) {
        this(resultCode.getCode(), message, resultCode.isRetryable(), resultCode.getCodeDescription(), cause);
    }

    public CommonException(ResultCode resultCode, String message, String codeDescription, Throwable cause) {
        this(resultCode.getCode(), message, resultCode.isRetryable(), codeDescription, cause);
    }

    public CommonException(int code, String message, boolean retryable, String codeDescription, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.retryable = retryable;
        this.codeDescription = codeDescription;
    }
}
