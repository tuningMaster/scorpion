package com.scorpion.config.impl.strategy;

import com.scorpion.common.utils.EnvUtils;

/**
 * BETA 策略：固定匹配所有（单例）
 */
public class BetaStrategy implements Strategy {
    public static final BetaStrategy INSTANCE = new BetaStrategy();

    @Override
    public boolean match(String param) {
        return EnvUtils.isBeta();
    }
}
