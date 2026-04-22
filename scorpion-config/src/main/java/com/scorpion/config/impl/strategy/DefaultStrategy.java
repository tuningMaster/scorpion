package com.scorpion.config.impl.strategy;

/**
 * 默认策略：固定匹配所有（兜底策略，单例）
 */
public class DefaultStrategy implements Strategy {
    public static final DefaultStrategy INSTANCE = new DefaultStrategy();

    @Override
    public boolean match(String param) {
        return true;
    }
}
