package com.scorpion.common.utils.invoke;

/**
 * 高性能反射 Getter 绑定接口
 */
@FunctionalInterface
public interface BindGetter<T, V> {
    V get(T target);

    /**
     * 创建始终返回常量值的 getter
     */
    static <T, V> BindGetter<T, V> constant(V value) {
        return target -> value;
    }

    /**
     * 通过字段名或方法名绑定 getter（优先查找 getXxx / isXxx，其次直接字段访问）
     */
    static <T, V> BindGetter<T, V> field(Class<T> targetClass, String fieldName, Class<V> valueType) {
        return BindGetters.field(targetClass, fieldName, valueType);
    }

    /**
     * 尝试作为方法或字段绑定 getter
     */
    static <T, V> BindGetter<T, V> fieldOrMethod(Class<T> targetClass, String name, Class<V> valueType) {
        return BindGetters.fieldOrMethod(targetClass, name, valueType);
    }
}
