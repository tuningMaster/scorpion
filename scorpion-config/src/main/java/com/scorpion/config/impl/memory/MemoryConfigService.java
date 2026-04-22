package com.scorpion.config.impl.memory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.scorpion.config.api.decoder.JSONDecoder;
import com.scorpion.config.api.holder.ConfigHolder;
import com.scorpion.config.impl.dal.ConfigDO;
import com.scorpion.config.impl.descriptor.ConfigDescriptor;
import com.scorpion.config.impl.descriptor.ConfigDescriptors;
import com.scorpion.config.impl.loader.ConfigLoadService;
import com.scorpion.config.facade.model.AppAllConfigDTO;
import com.scorpion.config.facade.model.ConfigDTO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 内存配置服务：初始化加载 + 定时重载 + 无锁替换
 */
@Slf4j
public class MemoryConfigService {
    /**
     * 开启配置监控（目前只有读配置监控）
     */
    private final boolean enableMonitor;

    /**
     * 配置加载服务
     */
    private final ConfigLoadService configLoadService;

    /**
     * reload 线程池
     */
    private ScheduledThreadPoolExecutor scheduler;

    /**
     * 内存配置实例
     */
    private volatile MemoryAppConfigs memoryAppConfigs;

    public MemoryConfigService(long reloadInterval, boolean enableMonitor, ConfigLoadService configLoadService) {
        this.enableMonitor = enableMonitor;
        this.configLoadService = configLoadService;

        // 创建空的 MemoryAppConfigs（version = -1）
        this.memoryAppConfigs = new MemoryAppConfigs(-1, Lists.newArrayList(), this.enableMonitor);

        // 创建 ScheduledThreadPoolExecutor（1 线程，命名 config-reload-pool-*）
        this.scheduler = new ScheduledThreadPoolExecutor(1,
                new ThreadFactoryBuilder().setNameFormat("config-reload-pool-%d").build());

        // 立即执行一次 reloadAppConfigs(init=true)
        reloadAppConfigs(true);

        // 注册定时任务：间隔 reloadInterval 毫秒，首次延迟随机 0~20 秒
        long initialDelay = new Random().nextInt(20000);
        scheduler.scheduleAtFixedRate(
                () -> reloadAppConfigs(false),
                initialDelay,
                reloadInterval,
                TimeUnit.MILLISECONDS
        );

        // 将 this 赋值给 ConfigHolder.CONFIGS
        ConfigHolder.INSTANCE = this;
    }

    /**
     * 重载配置
     */
    public void reloadAppConfigs(boolean init) {
        AppAllConfigDTO appAllConfigDTO = null;
        try {
            long currentVersion = Optional.ofNullable(this.memoryAppConfigs).map(MemoryAppConfigs::getAppConfigVersion)
                    .orElse(-1L);
            long currentCount = Optional.ofNullable(this.memoryAppConfigs).map(MemoryAppConfigs::getCount)
                    .orElse(0L);

            appAllConfigDTO = configLoadService.loadAndBackup(currentVersion, currentCount);
            if (appAllConfigDTO != null) {
                this.memoryAppConfigs = new MemoryAppConfigs(appAllConfigDTO.getVersion(), appAllConfigDTO.getConfigList(), this.enableMonitor);
            }
        } catch (Exception e) {
            // 除了第一次启动， 100%吃异常，避免任务无法重拾
            log.error("reloadAppConfigs error", e);
            // 初始化时要求必须要有配置（不是指有配置数据，而是至少有一个ConfigLoadService获取配置不为null），负责报错
            if (init && appAllConfigDTO == null) {
                throw e;
            }
        }
    }

    /**
     * 关闭
     */
    public void shutdown() {
        scheduler.shutdown();
        scheduler = null;
        memoryAppConfigs = null;
    }


    /**
     * 根据id查询实体
     * @param tClass 配置类型
     * @param param 灰度参数，通常为userid
     * @param id 配置id
     * @return 单个配置或者为null，为了减少gc，结果配置实例为共享内存，不可修改内容
     * @param <T> 配置类型
     */
    @Nonnull
    public <T> T findById(Class<T> tClass, String param, String id) {
        ConfigDescriptor<T> descriptor = ConfigDescriptors.getConfigDescriptorRequired(tClass);
        Objects.requireNonNull(this.memoryAppConfigs, "memoryAppConfigs is null, not initialized");

        return memoryAppConfigs.findById(param, descriptor.getType(), id);
    }

    /**
     * 根据id列表查询实体
     */
    @Nonnull
    public <T> List<T> findByIds(Class<T> tClass, String param, Collection<String> ids) {
        ConfigDescriptor<T> descriptor = ConfigDescriptors.getConfigDescriptorRequired(tClass);
        Objects.requireNonNull(this.memoryAppConfigs, "memoryAppConfigs is null, not initialized");
        return memoryAppConfigs.findByIds(param, descriptor.getType(), ids);
    }

    /**
     * 查询全部某个类型的配置
     */
    @Nonnull
    public <T> List<T> findAll(Class<T> tClass, String param) {
        ConfigDescriptor<T> descriptor = ConfigDescriptors.getConfigDescriptorRequired(tClass);
        Objects.requireNonNull(this.memoryAppConfigs, "memoryAppConfigs is null, not initialized");
        return memoryAppConfigs.findByType(param, descriptor.getType());
    }

    /**
     * 通过索引查询全部实体
     */
    @Nonnull
    public <T> List<T> findAllByIndex(Class<T> clazz, String param, String indexType, String indexKey) {
        ConfigDescriptor<T> descriptor = ConfigDescriptors.getConfigDescriptorRequired(clazz);
        Objects.requireNonNull(this.memoryAppConfigs, "memoryAppConfigs is null, not initialized");
        return memoryAppConfigs.findByIndex(param, descriptor.getType(), indexType, indexKey);
    }
}
