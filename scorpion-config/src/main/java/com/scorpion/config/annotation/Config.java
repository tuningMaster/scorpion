package com.scorpion.config.annotation;

import com.scorpion.config.api.decoder.ConfigDecoder;
import com.scorpion.config.api.decoder.JSONDecoder;
import com.scorpion.core.annotation.scan.CompileScanMeta;
import com.scorpion.core.processor.ResourceConstants;

import java.lang.annotation.*;

/**
 * 定义内存项目，配置项目在应用中仅支持读取
 */

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@CompileScanMeta(resourceFile = ResourceConstants.CONFIG_TYPES_FILE)
public @interface Config {
    /**
     * 配置类型标识
     */
    String type();

    /**
     * 配置id对应的字段名，用于JSONDecoder注入id
     */
    String idField();

    /**
     * 配置解码器，默认JSONDecoder
     */
    Class<? extends ConfigDecoder> decoder() default JSONDecoder.class;

    /**
     * 配置索引定义
     */
    ConfigIndex[] indices() default {};
}
