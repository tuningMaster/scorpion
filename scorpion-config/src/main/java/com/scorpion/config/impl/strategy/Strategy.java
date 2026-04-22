package com.scorpion.config.impl.strategy;

import com.scorpion.config.facade.enums.StrategyTypeEnum;
import com.scorpion.config.facade.model.StrategyDTO;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 灰度策略接口
 */
public interface Strategy {

    boolean match(String param);

    /**
     * 策略解析工厂方法
     * 空列表 → 抛异常
     * 单条 → parseOne 返回对应 Strategy
     * 多条 → 包装为 GroupStrategy
     */
    static Strategy parse(List<StrategyDTO> strategyList) {
        if (CollectionUtils.isEmpty(strategyList)) {
            throw new IllegalArgumentException("strategyList cannot be empty");
        }
        if (strategyList.size() == 1) {
            return parseOne(strategyList.get(0));
        }
        List<Strategy> strategies = strategyList.stream()
                .map(Strategy::parseOne)
                .collect(java.util.stream.Collectors.toList());
        return new GroupStrategy(strategies);
    }

    /**
     * 单条策略解析
     */
    static Strategy parseOne(StrategyDTO dto) {
        if (dto == null) {
            throw new RuntimeException("strategy is null");
        }

        StrategyTypeEnum type = StrategyTypeEnum.findByName(dto.getStrategyType());
        if (type == null) {
            throw new RuntimeException("strategyType not found: " + dto);
        }

        switch (Objects.requireNonNull(type)) {
            case WHITE_LIST:
                return new WhiteListStrategy(dto);
            case PERCENT:
                return new PercentStrategy(dto);
            case BETA:
                return BetaStrategy.INSTANCE;
            default:
                throw new IllegalArgumentException("未知策略类型:" + dto.getStrategyType());
        }
    }
}
