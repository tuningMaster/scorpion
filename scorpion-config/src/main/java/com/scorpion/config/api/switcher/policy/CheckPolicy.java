package com.scorpion.config.api.switcher.policy;

import com.scorpion.config.api.switcher.SwitcherConfig;

/**
 * 切流策略决策接口，用于 BOTH 模式下决定返回哪个结果
 */
public interface CheckPolicy {
    /**
     * 切流处于BOTH模式时，决策方法的返回值。这里除了决策返回外还能做核对逻辑
     * @param config 切流配置
     * @param newResult 新调用的结果
     * @param oldResult 老调用的结果
     * @param newThrowable 新调用的异常 成功调用时返回null
     * @param oldThrowable 老调用的异常 成功调用时返回null
     * @return 决策的实际返回值
     * @param <T> 返回值类型
     */
    <T> T doPolicy(SwitcherConfig config, T newResult, T oldResult, Throwable newThrowable, Throwable oldThrowable);
}
