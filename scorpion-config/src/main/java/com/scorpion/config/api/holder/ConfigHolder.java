package com.scorpion.config.api.holder;

import com.scorpion.config.impl.descriptor.ConfigDescriptor;
import com.scorpion.config.impl.descriptor.ConfigDescriptors;
import com.scorpion.config.impl.memory.MemoryAppConfigs;
import com.scorpion.config.impl.memory.MemoryConfig;
import com.scorpion.config.impl.memory.MemoryConfigService;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置访问静态入口，委托 MemoryConfigService 执行查询
 */
public class ConfigHolder {
    public static MemoryConfigService INSTANCE;

    /**
     * 按 id 精确查询
     * @param clazz 配置类型
     * @param param 灰度参数
     * @param id    配置 id
     * @return 匹配的配置内容（null 表示不存在）
     */
    public static <C> C findById(Class<C> clazz, String param, String id) {
        return INSTANCE.findById(clazz, param, id);
    }

    /**
     * 按 id 列表批量查询
     */
    public static <C> List<C> findByIds(Class<C> clazz, String param, List<String> ids) {
        return INSTANCE.findByIds(clazz, param, ids);
    }

    /**
     * 查询某类型全部配置
     */
    public static <C> List<C> findAll(Class<C> clazz, String param) {
        return INSTANCE.findAll(clazz, param);
    }

    /**
     * 按索引查询
     */
    public static <C> List<C> findAllByIndex(Class<C> clazz, String param, String indexType, String indexKey) {
        return INSTANCE.findAllByIndex(clazz, param, indexType, indexKey);
    }
}
