package com.scorpion.config.impl.strategy;

/**
 * 无策略：固定不匹配（策略解析失败时使用，单例）
 */
public class NoneStrategy implements Strategy {
    public static final NoneStrategy INSTANCE = new NoneStrategy();

    @Override
    public boolean match(String param) {
        return false;
    }
}
