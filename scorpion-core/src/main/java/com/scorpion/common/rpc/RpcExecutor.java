package com.scorpion.common.rpc;

/**
 * RPC 执行器函数式接口
 */
@FunctionalInterface
public interface RpcExecutor<REQ, RES> {
    RES execute(Object context, REQ req) throws Exception;
}
