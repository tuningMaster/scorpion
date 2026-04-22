package com.scorpion.config.annotation;


import java.lang.annotation.*;

/**
 * 内存配置索引
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigIndex {
    /**
     * 索引类型
     */
    String type();

    /**
     * 获取索引key的字段或者函数
     * 如果是字段需要有getter函数
     * 要求值为String
     * <p>
     *     当获取到的key为blank时，表示这条数据不需要这个类型的索引
     * </p>
     */
    String key();
}
