package com.scorpion.config.core;

import com.scorpion.common.exception.AssertUtils;
import com.scorpion.config.api.callback.ConfigCallbackFactory;
import com.scorpion.config.api.provider.ConfigProvider;
import com.scorpion.config.impl.dal.ConfigRepository;
import com.scorpion.config.impl.descriptor.ConfigDescriptors;
import com.scorpion.config.impl.loader.ConfigLoadService;
import com.scorpion.config.impl.memory.MemoryConfigService;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.List;

import static com.scorpion.common.exception.CommonResultCodeEnum.CONFIG_ERROR;

/**
 * 配置引擎，编排所有组件初始化
 */
@Slf4j
@Setter
public class ConfigEngine {

    private static final class InstanceHolder {
        static final ConfigEngine INSTANCE = new ConfigEngine();
    }

    public static ConfigEngine getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private MemoryConfigService memoryConfigService;

    /**
     * 重新加载间隔（单位毫秒），因为必须要加载配置才能使用，所以该字段必须大于0
     */
    private long reloadInterval;

    /**
     * 开启监控配置（目前只有读配置监控）
     */
    private boolean enableMonitor;

    /**
     * 配置服务提供列表
     */
    @NonNull
    private List<ConfigProvider> configProviderList;

    /**
     * DB配置repository
     */
    @NonNull
    private ConfigRepository configRepository;

    /**
     * 配置回调工厂
     */
    @NonNull
    private ConfigCallbackFactory configCallbackFactory;

    /**
     * 初始化配置引擎
     *
     * @param configProviderList 有序数据源列表
     * @param reloadInterval     重载间隔（毫秒）
     */
    public void init(List<ConfigProvider> configProviderList, long reloadInterval) {
        log.info("[ConfigEngine] start init...");
        ConfigServiceHolder holder = ConfigServiceHolder.getInstance();

        // 初始化 ConfigServiceHolder 所有字段
        holder.setConfigEngine(this);

        AssertUtils.isNotNull(configRepository, CONFIG_ERROR, "configRepository is required");
        holder.setConfigRepository(configRepository);

        // 1. ConfigDescriptors 配置描述工具初始化（扫描 @Config 类）
        ConfigDescriptors.init();
        holder.setConfigProviderList(configProviderList);

        // 2. 按 order 排序 provider
        AssertUtils.isNotEmpty(configProviderList, CONFIG_ERROR, "configProviderList is required");
        configProviderList.sort(Comparator.comparingInt(ConfigProvider::order));
        holder.setConfigProviderList(configProviderList);

        // 3. 创建 ConfigLoadService
        ConfigLoadService loadService = new ConfigLoadService(configProviderList);
        holder.setConfigLoadService(loadService);

        // 4. 创建 ConfigCallbackFactory
        AssertUtils.isTrue(reloadInterval > 0, CONFIG_ERROR, "reloadInterval must > 0!");
        MemoryConfigService memoryConfigService = new MemoryConfigService(reloadInterval, enableMonitor, loadService);
        holder.setMemoryConfigService(memoryConfigService);

        holder.setConfigCallbackFactory(configCallbackFactory);
        log.info("[ConfigEngine] init success!");
    }

    /**
     * 关闭配置引擎
     */
    public static void shutdown() {
        MemoryConfigService service = getInstance().memoryConfigService;
        if (service != null) {
            service.shutdown();
        }
    }
}
