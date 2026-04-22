package com.scorpion.config.impl.descriptor;

import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.scorpion.config.annotation.Config;
import com.scorpion.core.processor.ResourceConstants;
import com.scorpion.core.processor.ResourceProcessor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 配置描述注册表，扫描并注册所有 @Config 类
 */
public class ConfigDescriptors {
    private static ImmutableMap<String, ConfigDescriptor<?>> TYPE_DESCRIPTOR_MAP = ImmutableMap.of();
    private static ImmutableMap<Class<?>, ConfigDescriptor<?>> CLASS_DESCRIPTOR_MAP = ImmutableMap.of();

    /**
     * 初始化：扫描编译时扫描生成的 config-types 文件，反射加载类后注册
     */
    public static void init() {
        Set<String> setConfigClassNames = ResourceProcessor.load(
                Config.class.getClassLoader(),
                ResourceConstants.CONFIG_TYPES_FILE,
                new TypeReference<Set<String>>() {
                });
        List<Class<?>> configClassList = setConfigClassNames.stream().map(configClassName -> {
            try {
                return Class.forName(configClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        init(configClassList);

    }

    /**
     * 初始化：通过传入的配置类列表注册 ConfigDescriptor
     *
     * @param configClassList 标注了 @Config 的配置类列表
     */
    public static void init(List<Class<?>> configClassList) {
        Map<String, ConfigDescriptor<?>> configTypeDescriptorMap = Maps.newHashMap();
        Map<Class<?>, ConfigDescriptor<?>> classDescriptorMap = Maps.newHashMap();

        for (Class<?> clazz : configClassList) {
            ConfigDescriptor<?> descriptor = new ConfigDescriptor<>(clazz);
            String type = descriptor.getType();
            if (configTypeDescriptorMap.containsKey(type)) {
                throw new RuntimeException("Duplicate config type: " + type
                        + " (existing: " + configTypeDescriptorMap.get(type).getConfigClazz().getName()
                        + ", new: " + clazz.getName() + ")");
            }
            configTypeDescriptorMap.put(type, descriptor);
            classDescriptorMap.put(descriptor.getConfigClazz(), descriptor);
        }

        TYPE_DESCRIPTOR_MAP = ImmutableMap.copyOf(configTypeDescriptorMap);
        CLASS_DESCRIPTOR_MAP = ImmutableMap.copyOf(classDescriptorMap);
    }

    @SuppressWarnings("unchecked")
    public static <C> ConfigDescriptor<C> getConfigDescriptor(Class<C> clazz) {
        return (ConfigDescriptor<C>) CLASS_DESCRIPTOR_MAP.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <C> ConfigDescriptor<C> getConfigDescriptorRequired(Class<C> clazz) {
        ConfigDescriptor<C> descriptor = getConfigDescriptor(clazz);
        if (descriptor == null) {
            throw new IllegalStateException("Config descriptor not found for " + clazz.getName()
                    + ". Please check if the class is annotated with @Config and "
                    + "the compile-time annotation processor is properly configured in pom.xml.");
        }
        return descriptor;
    }

    public static ConfigDescriptor<?> getConfigDescriptor(String type) {
        return TYPE_DESCRIPTOR_MAP.get(type);
    }
}
