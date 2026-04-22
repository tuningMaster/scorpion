package com.scorpion.config.api.decoder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * 默认 JSON 解码器，自动注入 idField 字段值
 */
public class JSONDecoder<T> implements ConfigDecoder<T> {

    /**
     * id字段
     */
    private final String idField;

    /**
     * 构造
     */
    public JSONDecoder(String idField) {
        this.idField = idField;
    }

    @Override
    public T decode(String id, String content, Class<T> clazz) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        JSONObject json = JSON.parseObject(content);
        if (json == null) {
            return null;
        }

        if (StringUtils.isNotBlank(idField)) {
            json.put(idField, id);
        }

        return json.toJavaObject(clazz);
    }
}
