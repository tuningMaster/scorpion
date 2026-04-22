package com.scorpion.config.api.provider;

import com.scorpion.config.facade.model.AppAllConfigDTO;

/**
 * 配置数据源接口，支持多源容灾
 */
public interface ConfigProvider {

    /**
     * 优先级，越小越高
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

    /**
     * 加载全部 ONLINE 配置
     * @param currentVersion 当前版本号
     * @return 有新数据返回 AppAllConfigDTO，否则 null，失败抛异常
     */
    AppAllConfigDTO loadAppAllConfigs(long currentVersion);

    /**
     * 可选：备份配置
     */
    default void backupAppAllConfigs(AppAllConfigDTO appAllConfig) {
    }
}
