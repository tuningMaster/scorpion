package com.scorpion.config.api.switcher.policy;

import com.scorpion.config.api.switcher.SwitcherConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 默认切流策略：返回新逻辑结果
 */
@Slf4j
public class DefaultCheckPolicy implements CheckPolicy {
    public static final DefaultCheckPolicy INSTANCE = new DefaultCheckPolicy();

    @Override
    @SneakyThrows
    public <T> T doPolicy(SwitcherConfig config, T newResult, T oldResult, Throwable newThrowable, Throwable oldThrowable) {
        if (newThrowable != null && oldThrowable != null) {
            // 合并异常向外抛出
            oldThrowable.addSuppressed(newThrowable);
            throw oldThrowable;
        }

        if (oldThrowable != null) {
            throw oldThrowable;
        }

        if (newThrowable != null) {
            throw newThrowable;
        }

        boolean checkOk = Objects.equals(newResult, oldResult);
        if (!checkOk) {
            log.warn("[DefaultCheckPolicy] doPolicy check fail. new: {}, old: {}", newResult, oldResult);
        }

        if (config.isMonitorEnable()) {
            // 打点上报
        }

        // 默认策略BOTH模式下返回的是old
        return oldResult;
    }
}
