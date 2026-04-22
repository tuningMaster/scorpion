package com.scorpion.config.impl.strategy;

import com.google.common.collect.Sets;
import com.scorpion.common.constants.Constants;
import com.scorpion.config.facade.model.StrategyDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * 白名单策略：param 在逗号分隔的白名单中
 */
public class WhiteListStrategy implements Strategy {

    private final Set<String> whiteList;

    public WhiteListStrategy(StrategyDTO strategy) {
        if (StringUtils.isBlank(strategy.getWhiteList())) {
            throw new RuntimeException("whiteList is blank");
        }

        whiteList = Sets.newHashSet(strategy.getWhiteList().split(Constants.COMMA));
    }

    @Override
    public boolean match(String param) {
        return whiteList.contains(param);
    }
}
