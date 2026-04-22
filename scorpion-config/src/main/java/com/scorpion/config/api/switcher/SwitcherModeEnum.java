package com.scorpion.config.api.switcher;

/**
 * 切流模式枚举
 */
public enum SwitcherModeEnum {
    OLD,   // 只执行旧逻辑
    NEW,   // 只执行新逻辑
    BOTH   // 两者都执行，由 CheckPolicy 决策返回哪个
}
