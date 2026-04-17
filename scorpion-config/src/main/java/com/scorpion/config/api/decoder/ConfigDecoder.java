package com.scorpion.config.api.decoder;

/**
 * 自定义配置解析器
 *
 */
public interface ConfigDecoder<T> {

    /**
     * decode配置数据，结果的id自动根据注解配置填充，无需解析
     * @param id         配置id
     * @param content    配置内容（注意：content为null时，直接就解析为null，不需要经过decode）
     * @param clazz      配置类型
     * @return 结果
     */
    T decode(String id, String content, Class<T> clazz);
}
