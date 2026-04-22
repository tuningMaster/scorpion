package com.scorpion.common.exception;

/**
 * 参数校验失败异常
 */
public class ParamsInvalidException extends CommonException {

    public ParamsInvalidException(ResultCode resultCode) {
        super(resultCode);
    }

    public ParamsInvalidException(String message) {
        super(CommonResultCodeEnum.PARAMETER_ILLEGAL, message);
    }

    public ParamsInvalidException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public ParamsInvalidException(ResultCode resultCode, String message, Throwable cause) {
        super(resultCode, message, cause);
    }

    public ParamsInvalidException(int code, String message, boolean retryable, String codeDescription, Throwable cause) {
        super(code, message, retryable, codeDescription, cause);
    }
}
