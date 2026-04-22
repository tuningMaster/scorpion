package com.scorpion.config.facade.rpc;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * RPC层策略配置组合
 */
@Data
public class RpcStrategyConfigDTO implements Serializable {

    private List<RpcStrategyDTO> strategyList;

    @ToString.Exclude
    private String content;
}
