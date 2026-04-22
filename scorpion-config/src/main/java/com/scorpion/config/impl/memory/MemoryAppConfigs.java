package com.scorpion.config.impl.memory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.scorpion.config.facade.model.ConfigDTO;
import com.scorpion.config.impl.descriptor.ConfigDescriptor;
import com.scorpion.config.impl.descriptor.ConfigDescriptors;
import com.scorpion.config.impl.descriptor.NoneTypeConfigDescriptor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 维护三个 ImmutableSortedMap 的配置快照
 * 通过引用替换保证读安全
 */
@Slf4j
public class MemoryAppConfigs {
    /**
     * 应用配置版本
     */
    @Getter
    private final long appConfigVersion;

    /**
     * 应用的配置总数
     */
    @Getter
    private final long count;

    /**
     * 是够开启监控
     */
    private final boolean enableMonitor;


    /**
     * 不存在ConfigDescriptor的map，目前这个map提供给前端使用
     * 前端用的配置map(目前的区分方式是：业务代码中没有对应的ConfigDescriptor则放到这个里，存在风险，后续需要在业务配置表加字段隔离)
     */
    private final ImmutableSortedMap<MemoryConfigIndex, MemoryConfig<JSONObject>> unKnownMap;


    /**
     * 后端用的配置map
     * id管理配置的map,idMap里，一个数据确保只有一个key，结果不用使用id去重
     */
    private final ImmutableSortedMap<MemoryConfigIndex, MemoryConfig<?>> idMap;

    /**
     * 索引关联配置的map，索引map里，一条config对应多个item，每个item相同的indexType下indexKey不同，可能存在多条，结果需要去重
     */
    private final ImmutableSortedMap<MemoryConfigIndex, MemoryConfig<?>> indexMap;


    /**
     * 配置描述类
     */
    public MemoryAppConfigs(long version, List<ConfigDTO> list, boolean enableMonitor) {
        this.enableMonitor = enableMonitor;

        Map<MemoryConfigIndex, MemoryConfig<JSONObject>> unKnownMap = new HashMap<>(list.size());
        Map<MemoryConfigIndex, MemoryConfig<?>> idMap = new HashMap<>(list.size());
        Map<MemoryConfigIndex, MemoryConfig<?>> indexMap = Maps.newHashMap();

        for (ConfigDTO dto : list) {
            ConfigDescriptor<?> descriptor = ConfigDescriptors.getConfigDescriptor(dto.getType());
            if (descriptor == null) {
                // 兼容这种情况，方便下线配置，回滚代码，前端消费config等场景
                log.debug("unknownType config, dto: {}", dto);
                MemoryConfig<JSONObject> rawConfig = new NoneTypeConfigDescriptor(dto.getType()).toMemory(dto);
                if (rawConfig != null) {
                    unKnownMap.put(idIndex(rawConfig.getType(), rawConfig.getId()), rawConfig);
                }
                continue;
            }

            MemoryConfig<?> memoryConfig = descriptor.toMemory(dto);
            if (memoryConfig == null) {
                continue;
            }

            // id
            idMap.put(idIndex(memoryConfig.getType(), memoryConfig.getId()), memoryConfig);

            // 处理索引
            for (MemoryStrategyConfig<?> strategyConfig : memoryConfig.getStrategyConfigs()) {
                for (MemoryConfigIndex index : strategyConfig.getIndices()) {
                    indexMap.put(index, memoryConfig);
                }
            }
        }

        this.count = list.size();
        this.appConfigVersion = version;
        this.unKnownMap = ImmutableSortedMap.copyOf(unKnownMap);
        this.idMap = ImmutableSortedMap.copyOf(idMap);
        this.indexMap = ImmutableSortedMap.copyOf(indexMap);

        log.info("parseConfigToMemory rawCnt: {}, version: {}, unKnownCount: {}, parseCnt: {}, indexCnt: {}",
                this.count, this.appConfigVersion, this.unKnownMap.size(), this.idMap.size(), this.indexMap.size());
    }

    /**
     * 按 id 精确查询
     */
    @SuppressWarnings("unchecked")
    public <T> T findById(String param, String type, String id) {
        MemoryConfig<T> memoryConfig = (MemoryConfig<T>) idMap.get(idIndex(type, id));
        if (memoryConfig == null) {
            return null;
        }

        T result = memoryConfig.getConfigSelector().select(param, null);
        monitorReport(type, "findById", result == null ? 0 : 1);
        return result;
    }

    /**
     * 通过id批量查找
     */
    public <T> List<T> findByIds(String param, String configType, Collection<String> configIds) {
        List<T> list = new ArrayList<>(configIds.size());
        for (String configId : configIds) {
            MemoryConfig<T> memoryConfig = (MemoryConfig<T>) idMap.get(idIndex(configType, configId));
            if (memoryConfig == null) {
                continue;
            }

            T item = memoryConfig.getConfigSelector().select(param, null);
            if (item != null) {
                list.add(item);
            }
        }
        monitorReport(configType, "findByIds", list.size());
        return list;
    }

    /**
     * 通过类型查找
     */
    public <T> List<T> findByType(String param, String configType) {
        Range<MemoryConfigIndex> range = MemoryConfigIndex.prefixMatchRange(configType);
        ImmutableSortedMap<MemoryConfigIndex, MemoryConfig<?>> matches = idMap.subMap(range.getMinimum(), range.getMaximum());

        List<T> list = new ArrayList<>(matches.size());
        for (MemoryConfig<?> value : matches.values()) {
            T item = (T) value.getConfigSelector().select(param, null);
            if (item != null) {
                list.add(item);
            }
        }

        monitorReport(configType, "findByType", list.size());
        return list;
    }


    /**
     * 通过索引查找
     * @param param 灰度参数，通常为userId
     * @param configType 配置类型
     * @param indexType 索引类型
     * @param indexKey 索引key
     * @return 全部配置，可空、非null、无序
     * @param <T> 配置类型
     */
    public <T> List<T> findByIndex(String param, String configType, String indexType, String indexKey) {
        Range<MemoryConfigIndex> range = MemoryConfigIndex.prefixMatchRange(configType, indexType, indexKey);
        ImmutableSortedMap<MemoryConfigIndex, MemoryConfig<?>> matches = indexMap.subMap(range.getMinimum(), range.getMaximum());
        Set<String> unique = Sets.newHashSetWithExpectedSize(matches.size());
        List<T> list = new ArrayList<>(matches.size());

        for (MemoryConfig<?> value : matches.values()) {
            if (unique.contains(value.getId())) {
                continue;
            }

            unique.add(value.getId());
            T item = (T) value.getConfigSelector().select(param, range);
            if (item != null) {
                list.add(item);
            }
        }

        monitorReport(configType, "findByIndex", list.size());

        return list;
    }


    /**
     * 获取id的索引
     * @param configType 配置类型
     * @param configId 配置id
     * @return 结果
     */
    private MemoryConfigIndex idIndex(String configType, String configId) {
        return MemoryConfigIndex.builder()
                .configType(configType)
                .configId(configId)
                .build();
    }

    private void monitorReport(String type, String action, int count) {
        if (!enableMonitor) {
            return;
        }

        // 打点监控
    }
}


