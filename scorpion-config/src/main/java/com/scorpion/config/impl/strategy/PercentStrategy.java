package com.scorpion.config.impl.strategy;

import com.google.common.base.Strings;
import com.scorpion.config.facade.model.StrategyDTO;

/**
 * 百分比策略：abs(param.hashCode() % 100) < percent（短 key 会 repeat 3 次）
 */
public class PercentStrategy implements Strategy {
    /**
     * 最小长度
     */
    private static final int KEY_MIN_SIZE = 2;

    /**
     * 100
     */
    private static final int HUNDRED = 100;

    /**
     * 白名单
     */
    private final int percent;

    public PercentStrategy(StrategyDTO strategy) {
        if (strategy.getPercent() == null) {
            throw new RuntimeException("percent is null");
        }

        if (strategy.getPercent() < 0 || strategy.getPercent() > 100) {
            throw new RuntimeException("percent is illegal, percent: " + strategy.getPercent());
        }

        this.percent = strategy.getPercent();
    }

    @Override
    public boolean match(String param) {
        /**
         * 这里需要注意，如果key长度较小，比如1和2,而base较大，生成的hashCode有可能永远命中不了
         * 因此这里进行了一定的处理，将key重复3次以满足条件
         */
        if (param.length() <= KEY_MIN_SIZE) {
            param = Strings.repeat(param, 3);
        }

        return Math.abs(param.hashCode() % HUNDRED) < percent;
    }
}
