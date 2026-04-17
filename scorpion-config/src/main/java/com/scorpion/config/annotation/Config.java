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
    String type();

    String idField();

    Class<? extends ConfigDecoder> decoder() default JSONDecoder.class;
}
