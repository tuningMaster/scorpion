package com.scorpion.common.exception;

/**
 * 系统逻辑异常
 */
public class SystemLogicException extends CommonException {

    public SystemLogicException(ResultCode resultCode) {
        super(resultCode);
    }

    public SystemLogicException(String message) {
        super(CommonResultCodeEnum.SYSTEM_ERROR, message);
    }

    public SystemLogicException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public SystemLogicException(ResultCode resultCode, String message, Throwable cause) {
        super(resultCode, message, cause);
    }

    public SystemLogicException(int code, String message, boolean retryable, String codeDescription, Throwable cause) {
        super(code, message, retryable, codeDescription, cause);
    }
}
