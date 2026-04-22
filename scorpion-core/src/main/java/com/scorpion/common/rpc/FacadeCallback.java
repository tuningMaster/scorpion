package com.scorpion.common.rpc;

/**
 * Facade 回调接口，业务方实现此接口来定义具体处理逻辑
 */
public interface FacadeCallback<REQ, RES> {

    /**
     * 方法标识，用于日志前缀
     */
    String identifier();

    /**
     * 参数校验，默认空实现
     */
    default void checkParameters(REQ request) {
    }

    /**
     * 业务逻辑
     */
    RES execute(REQ request);
}
