package com.scorpion.config.impl.descriptor;

import com.alibaba.fastjson.JSON;
import com.scorpion.config.annotation.Config;
import com.scorpion.config.annotation.ConfigIndex;
import com.scorpion.config.api.decoder.ConfigDecoder;
import com.scorpion.config.api.decoder.JSONDecoder;
import com.scorpion.config.facade.model.ConfigDTO;
import com.scorpion.config.facade.model.StrategyConfigDTO;
import com.scorpion.config.impl.memory.MemoryConfig;
import com.scorpion.config.impl.memory.MemoryConfigIndex;
import com.scorpion.config.impl.memory.MemoryConfigSelector;
import com.scorpion.config.impl.memory.MemoryStrategyConfig;
import com.scorpion.config.impl.strategy.DefaultStrategy;
import com.scorpion.config.impl.strategy.NoneStrategy;
import com.scorpion.config.impl.strategy.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 每个 @Config 类对应一个 ConfigDescriptor，封装配置类型、解码器、索引描述
 */
public class ConfigDescriptor<C> {

    private static final Logger log = LoggerFactory.getLogger(ConfigDescriptor.class);

    private final Class<C> configClazz;
    private final String type;
    private final ConfigDecoder<C> decoder;
    private final Map<String, ConfigIndexDescriptor<C>> indices;
    private final String idField;

    public ConfigDescriptor(Class<C> configClazz) {
        this.configClazz = configClazz;
        Config configAnnotation = configClazz.getAnnotation(Config.class);
        if (configAnnotation == null) {
            throw new IllegalArgumentException(configClazz.getName() + " is not annotated with @Config");
        }
        this.type = configAnnotation.type();
        this.idField = configAnnotation.idField();

        // decoder 特殊处理：如果是默认的 JSONDecoder，需要传入 idField 参数
        Class<? extends ConfigDecoder> decoderClass = configAnnotation.decoder();
        try {
            if (decoderClass == JSONDecoder.class) {
                this.decoder = new JSONDecoder<>(idField);
            } else {
                this.decoder = (ConfigDecoder<C>) decoderClass.getDeclaredConstructor().newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to instantiate decoder for " + configClazz.getName(), e);
        }

        // 解析索引
        ConfigIndex[] configIndices = configAnnotation.indices();
        if (configIndices != null && configIndices.length > 0) {
            Map<String, ConfigIndexDescriptor<C>> indexMap = new LinkedHashMap<>();
            for (ConfigIndex index : configIndices) {
                ConfigIndexDescriptor<C> descriptor = new ConfigIndexDescriptor<>(index, configClazz);
                indexMap.put(index.type(), descriptor);
            }
            this.indices = Collections.unmodifiableMap(indexMap);
        } else {
            this.indices = Collections.emptyMap();
        }
    }

    /**
     * DTO → 内存模型，包含策略配置解析
     */
    public MemoryConfig<C> toMemory(ConfigDTO configDTO) {
        C defaultConfig = decode(configDTO.getCustomId(), configDTO.getContent());

        List<MemoryStrategyConfig<C>> strategyConfigs = new ArrayList<>();

        // 解析策略配置
        if (configDTO.getStrategyConfigs() != null) {
            for (StrategyConfigDTO strategyConfigDTO : configDTO.getStrategyConfigs()) {
                C config = decode(configDTO.getCustomId(), strategyConfigDTO.getContent());
                if (config == null) {
                    continue;
                }

                Strategy strategy;
                try {
                    strategy = Strategy.parse(strategyConfigDTO.getStrategyList());
                } catch (Exception e) {
                    log.error("Failed to parse strategy for config {}-{}, using NoneStrategy",
                            configDTO.getCustomId(), type);
                    strategy = NoneStrategy.INSTANCE;
                }

                // 构建索引集合
                Set<MemoryConfigIndex> indexSet = new HashSet<>();
                for (ConfigIndexDescriptor<C> indexDescriptor : indices.values()) {
                    MemoryConfigIndex idx = indexDescriptor.toIndex(config, type, configDTO.getCustomId());
                    if (idx != null) {
                        indexSet.add(idx);
                    }
                }

                strategyConfigs.add(MemoryStrategyConfig.<C>builder()
                        .strategy(strategy)
                        .config(config)
                        .indices(indexSet)
                        .build());
            }
        }

        // 兜底策略：最后必定是 DefaultStrategy
        strategyConfigs.add(MemoryStrategyConfig.<C>builder()
                .strategy(DefaultStrategy.INSTANCE)
                .config(defaultConfig != null ? defaultConfig : createEmptyInstance())
                .indices(new HashSet<>())
                .build());

        MemoryConfigSelector<C> selector = MemoryConfigSelector.build(strategyConfigs);

        return MemoryConfig.<C>builder()
                .type(type)
                .id(configDTO.getCustomId())
                .strategyConfigs(strategyConfigs)
                .configSelector(selector)
                .build();
    }

    /**
     * 内容解析，异常返回 null 不中断
     */
    public C decode(String id, String content) {
        if (content == null) {
            return null;
        }
        try {
            return decoder.decode(id, content, configClazz);
        } catch (Exception e) {
            log.error("Failed to decode config {}: {}", type, e.getMessage());
            return null;
        }
    }

    /**
     * 策略配置项解析
     */
    public C decodeStrategyItem(String id, StrategyConfigDTO strategyConfigDTO) {
        return decode(id, strategyConfigDTO.getContent());
    }

    /**
     * 默认配置项解析
     */
    public C decodeDefaultItem(ConfigDTO configDTO) {
        return decode(configDTO.getCustomId(), configDTO.getContent());
    }

    /**
     * 创建空实例（兜底用）
     */
    private C createEmptyInstance() {
        try {
            return configClazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create empty instance for " + configClazz.getName(), e);
        }
    }

    public Class<C> getConfigClazz() {
        return configClazz;
    }

    public String getType() {
        return type;
    }

    public ConfigDecoder<C> getDecoder() {
        return decoder;
    }

    public Map<String, ConfigIndexDescriptor<C>> getIndices() {
        return indices;
    }

    public String getIdField() {
        return idField;
    }
}
