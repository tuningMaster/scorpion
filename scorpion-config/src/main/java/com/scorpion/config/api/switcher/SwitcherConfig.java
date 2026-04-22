package com.scorpion.config.api.switcher;

import com.scorpion.config.annotation.Config;
import lombok.Data;

/**
 * 切流配置类，用于 Switchers 工具类
 */
@Data
@Config(type = "switcher", idField = "id")
public class SwitcherConfig {
    /**
     * 切流id
     */
    private String id;

    /**
     * 切流模式
     */
    private SwitcherModeEnum mode;         // OLD / NEW / BOTH

    /**
     * 是否开启监控上报
     */
    private boolean monitorEnable;
}
