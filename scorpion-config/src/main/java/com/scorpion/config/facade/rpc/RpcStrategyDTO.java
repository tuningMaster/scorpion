package com.scorpion.config.facade.rpc;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * RPC层单条策略规则
 */
@Data
public class RpcStrategyDTO implements Serializable {

    private String strategyType;
    private String whiteList;
    private Integer percent;
}
