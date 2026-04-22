package com.scorpion.config.core;

import com.scorpion.config.api.callback.ConfigCallbackFactory;
import com.scorpion.config.api.provider.ConfigProvider;
import com.scorpion.config.impl.dal.ConfigRepository;
import com.scorpion.config.impl.loader.ConfigLoadService;
import com.scorpion.config.impl.memory.MemoryConfigSelector;
import com.scorpion.config.impl.memory.MemoryConfigService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 配置服务上下文，持有全局共享的配置加载服务实例
 */
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigServiceHolder {

    private static final class InstanceHolder {
        static final ConfigServiceHolder INSTANCE = new ConfigServiceHolder();
    }

    public static ConfigServiceHolder getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * 配置引擎
     */
    private ConfigEngine configEngine;

    /**
     * 配置DAO
     */
    private ConfigRepository configRepository;

    /**
     * 配置加载服务
     */
    private ConfigLoadService configLoadService;

    /**
     * 内存配置服务
     */
    private MemoryConfigService memoryConfigService;

    /**
     * 配置提供
     */
    private List<ConfigProvider> configProviderList;

    /**
     * 配置回调工厂
     */
    private ConfigCallbackFactory configCallbackFactory;
}
