package com.scorpion.config.impl.descriptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.scorpion.config.api.decoder.ConfigDecoder;
import com.scorpion.config.facade.model.ConfigDTO;
import com.scorpion.config.facade.model.StrategyConfigDTO;
import com.scorpion.config.impl.memory.MemoryConfig;
import com.scorpion.config.impl.memory.MemoryConfigSelector;
import com.scorpion.config.impl.memory.MemoryStrategyConfig;
import com.scorpion.config.impl.strategy.DefaultStrategy;
import com.scorpion.config.impl.strategy.NoneStrategy;
import com.scorpion.config.impl.strategy.Strategy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * java代码中不存在对应类型的@Config，则会有一个兜底的配置描述，用于前端读取配置的场景。类型为{@link com.alibaba.fastjson.JSONObject}
 */
@Slf4j
@Getter
public class NoneTypeConfigDescriptor {
    /**
     * model 类型
     */
    private final String type;

    /**
     * 配置decoder
     */
    private final ConfigDecoder<JSONObject> decoder;

    /**
     * 构建config的descriptor
     * @param type 配置type
     */
    public NoneTypeConfigDescriptor(String type) {
        this.type = type;
        this.decoder = (id, content, clazz) -> JSON.parseObject(content);
    }

    /**
     * 转化为内存模型
     */
    public MemoryConfig<JSONObject> toMemory(ConfigDTO dto) {
        try {
            List<MemoryStrategyConfig<JSONObject>> strategyConfigs = new ArrayList<>();

            if (dto.getStrategyConfigs() != null) {
                for (StrategyConfigDTO strategyConfig : dto.getStrategyConfigs()) {
                    strategyConfigs.add(decodeStrategyItem(dto.getCustomId(), strategyConfig));
                }
            }

            strategyConfigs.add(decodeDefaultItem(dto));

            return MemoryConfig.<JSONObject>builder()
                    .type(type)
                    .id(dto.getCustomId())
                    .strategyConfigs(strategyConfigs)
                    .configSelector(MemoryConfigSelector.build(strategyConfigs))
                    .build();
        } catch (RuntimeException e) {
            log.error("parse config error, dto: {}", dto, e);
            return null;
        }
    }


    /**
     * 解析默认配置项
     */
    private MemoryStrategyConfig<JSONObject> decodeDefaultItem(ConfigDTO dto) {
        JSONObject config = decode(dto.getCustomId(), dto.getContent());
        return MemoryStrategyConfig.<JSONObject>builder()
                .strategy(DefaultStrategy.INSTANCE)
                .config(config)
                .indices(Collections.emptySet())
                .build();
    }

    public JSONObject decode(String id, String content) {
        try {
            if (content == null) {
                return null;
            }

            return decoder.decode(id, content, null);
        } catch (RuntimeException e) {
            log.error("parse content error, type: {}, id: {}, content: {}", type, id, content, e);
            return null;
        }
    }


    /**
     * 解析策略配置项
     */
    private MemoryStrategyConfig<JSONObject> decodeStrategyItem(String id, StrategyConfigDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getStrategyList())) {
            return null;
        }

        Strategy strategy = NoneStrategy.INSTANCE;
        try {
            strategy = Strategy.parse(dto.getStrategyList());
        } catch (RuntimeException e) {
            log.error("parse strategy error, type: {}, id: {}, strategy: {}", type, id, dto.getStrategyList(), e);
        }

        JSONObject config = decode(id, dto.getContent());

        return MemoryStrategyConfig.<JSONObject>builder()
                .strategy(strategy)
                .config(config)
                .indices(Collections.emptySet())
                .build();
    }
}

