package com.scorpion.config.impl.memory;

import org.apache.commons.lang3.Range;
import lombok.Builder;
import lombok.Value;

import java.util.Objects;

/**
 * 统一索引键，用于 SortedMap 排序
 * 排序规则: configType → indexType → indexKey → configId，null 排在最前
 */
@Value
@Builder(toBuilder = true)
public class MemoryConfigIndex implements Comparable<MemoryConfigIndex> {
    private static final String MAX_STRING = "\uFFFF";

    /**
     * 配置类型
     */
    String configType;

    /**
     * 索引类型
     */
    String indexType;

    /**
     * 索引key
     */
    String indexKey;

    /**
     * 配置id
     */
    String configId;

    @Override
    public int compareTo(MemoryConfigIndex o) {
        if (o == null) {
            // 这个兜底，理论上不会
            return -1;
        }

        // configType
        int result = compare(this.getConfigType(), o.getConfigType());
        if (result != 0) {
            return result;
        }

        // indexType
        result = compare(this.getIndexType(), o.getIndexType());
        if (result != 0) {
            return result;
        }

        // index key
        result = compare(this.getIndexKey(), o.getIndexKey());
        if (result != 0) {
            return result;
        }

        // config id 兜底排序
        return compare(this.getConfigId(), o.getConfigId());
    }

    /**
     * 对比排序
     * @param a 字符串a
     * @param b 字符串b
     * @return 结果
     */
    private static int compare(final String a, final String b) {
        if (Objects.equals(a, b)) {
            return 0;
        }

        if (a == null) {
            return -1;
        }

        if (b == null) {
            return 1;
        }

        return a.compareTo(b);
    }

    /**
     * 前缀搜索区间
     */
    public static Range<MemoryConfigIndex> prefixMatchRange(String configType) {
        Objects.requireNonNull(configType, "configType not null");
        MemoryConfigIndex start = MemoryConfigIndex.builder()
                .configType(configType)
                .build();

        return Range.between(start, start.toBuilder().configId(MAX_STRING).build());
    }

    /**
     * 前缀搜索区间
     */
    public static Range<MemoryConfigIndex> prefixMatchRange(String configType, String indexType, String indexKey) {
        Objects.requireNonNull(configType, "configType not null");
        Objects.requireNonNull(indexType, "indexType not null");
        Objects.requireNonNull(indexKey, "indexKey not null");

        MemoryConfigIndex start = MemoryConfigIndex.builder()
                .configType(configType)
                .indexType(indexType)
                .indexKey(indexKey)
                .build();

        return Range.between(start, start.toBuilder().indexKey(indexKey).configId(MAX_STRING).build());
    }
}
