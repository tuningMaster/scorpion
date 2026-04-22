package com.scorpion.config.api.callback;

import com.google.common.collect.Lists;
import com.scorpion.common.exception.CommonException;
import com.scorpion.config.facade.model.ConfigDTO;
import com.scorpion.config.facade.model.StrategyConfigDTO;
import com.scorpion.config.impl.descriptor.ConfigDescriptor;
import com.scorpion.config.impl.descriptor.ConfigDescriptors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Null;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * 配置变更回调接口
 * 业务方实现此接口，在配置变更时进行校验/加工/触发副作用
 */
public interface ConfigChangeCallback<C> {
    /**
     * 配置变更回调方法
     * 抛出 CommonException 会回显到配置平台前端
     *
     * @param context    回调上下文
     * @param configDTO  配置传输对象
     * @return 加工后的 ConfigDTO
     */
    ConfigDTO onChange(CallbackContext context, ConfigDTO configDTO);


    default Class<C> getConfigClass() {
        try {
            return (Class<C>) ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
        } catch (ClassCastException e) {
            throw new CommonException("编码错误，请检查代码", e);
        }
    }

    /**
     * 解析全量配置
     * @param configDTO 配置dto
     * @return 全量配置
     */
    @Nullable
    default C decodeDefaultConfig(ConfigDTO configDTO) {
        ConfigDescriptor<C> descriptor = ConfigDescriptors.getConfigDescriptorRequired(getConfigClass());
        return descriptor.decode(configDTO.getCustomId(), configDTO.getContent());
    }

    /**
     * 解析策略配置
     * @param configDTO 配置DTO
     * @return 策略配置列表
     */
    @Nonnull
    default List<C> decodeStrategyConfigs(ConfigDTO configDTO) {
        ConfigDescriptor<C> descriptor = ConfigDescriptors.getConfigDescriptorRequired(getConfigClass());
        if (CollectionUtils.isEmpty(configDTO.getStrategyConfigs())) {
            return Lists.newArrayList();
        }

        List<C> ret = Lists.newArrayListWithCapacity(configDTO.getStrategyConfigs().size());
        for (StrategyConfigDTO strategyConfig : configDTO.getStrategyConfigs()) {
            ret.add(descriptor.decode(configDTO.getCustomId(), strategyConfig.getContent()));
        }

        return ret;
    }
}
