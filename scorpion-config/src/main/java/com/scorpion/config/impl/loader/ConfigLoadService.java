package com.scorpion.config.impl.loader;

import com.scorpion.config.api.provider.ConfigProvider;
import com.scorpion.config.facade.model.AppAllConfigDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 配置加载服务：多源容灾加载
 * 按 order 顺序遍历 ConfigProvider 列表，先从主数据源加载，失败时自动降级到备份数据源
 */
@Slf4j
@AllArgsConstructor
public class ConfigLoadService {
    private final List<ConfigProvider> configProviderList;

    /**
     * 加载并备份配置
     *
     * @param currentVersion 当前版本号
     * @param currentCount   当前配置数量
     * @return 成功返回新配置，全部失败返回 null
     */
    public AppAllConfigDTO loadAndBackup(long currentVersion, long currentCount) {
        for (ConfigProvider provider : configProviderList) {
            try {
                AppAllConfigDTO appAllConfig = provider.loadAppAllConfigs(currentVersion);
                if (appAllConfig != null) {
                    // 加载成功，执行 backup
                    provider.backupAppAllConfigs(appAllConfig);
                    return appAllConfig;
                }
            } catch (Exception e) {
                log.error("loadAndBackup from {} error. currentVersion: {}, currentCount: {}",
                        provider.getClass().getSimpleName(), currentVersion, currentCount);
            }
        }
        return null;
    }
}
