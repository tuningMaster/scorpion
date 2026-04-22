package com.scorpion.config.impl.descriptor;

import com.scorpion.config.annotation.ConfigIndex;
import com.scorpion.config.impl.memory.MemoryConfigIndex;
import org.apache.commons.lang3.StringUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * 配置索引描述，封装单个 @ConfigIndex 的元信息
 */
public class ConfigIndexDescriptor<C> {

    private final String indexType;
    private final MethodHandle indexKeyGetter;

    public ConfigIndexDescriptor(ConfigIndex configIndex, Class<C> configClazz) {
        this.indexType = configIndex.type();
        this.indexKeyGetter = resolveGetter(configIndex.key(), configClazz);
    }

    private MethodHandle resolveGetter(String key, Class<C> configClazz) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            String getterName = key;
            // 如果是字段名，尝试找对应的 getter
            if (getterName.startsWith("get")) {
                // already a getter name
            } else {
                getterName = "get" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
            }
            return lookup.findVirtual(configClazz, getterName, MethodType.methodType(String.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Failed to resolve index key getter '" + key + "' for class " + configClazz.getName(), e);
        }
    }

    /**
     * 将配置对象转换为 MemoryConfigIndex
     * 当 indexKey 为空时返回 null（该条数据不建索引）
     */
    public MemoryConfigIndex toIndex(C config, String configType, String configId) {
        if (indexKeyGetter == null) {
            return null;
        }
        try {
            String indexKey = (String) indexKeyGetter.invoke(config);
            if (StringUtils.isBlank(indexKey)) {
                return null;
            }
            return MemoryConfigIndex.builder()
                    .configType(configType)
                    .indexType(indexType)
                    .indexKey(indexKey)
                    .configId(configId)
                    .build();
        } catch (Throwable e) {
            return null;
        }
    }

    public String getIndexType() {
        return indexType;
    }
}
