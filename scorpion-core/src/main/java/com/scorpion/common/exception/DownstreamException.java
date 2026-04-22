package com.scorpion.common.exception;

/**
 * 下游调用失败异常
 */
public class DownstreamException extends CommonException {

    public DownstreamException(ResultCode resultCode) {
        super(resultCode);
    }

    public DownstreamException(String message) {
        super(CommonResultCodeEnum.INTEGRATION_ERROR, message);
    }

    public DownstreamException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public DownstreamException(ResultCode resultCode, String message, Throwable cause) {
        super(resultCode, message, cause);
    }

    public DownstreamException(int code, String message, boolean retryable, String codeDescription, Throwable cause) {
        super(code, message, retryable, codeDescription, cause);
    }
}
