package com.scorpion.config.api.callback;

import com.scorpion.common.exception.AssertUtils;
import com.scorpion.config.annotation.Config;
import com.scorpion.config.facade.enums.ConfigChangeTypeEnum;
import com.scorpion.config.facade.enums.OperateTypeEnum;
import com.scorpion.config.facade.model.ConfigDTO;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static com.scorpion.common.exception.CommonResultCodeEnum.SYSTEM_ERROR;

/**
 * 配置回调工厂，提供便捷方法解析配置
 */
@Slf4j
@Setter
public class ConfigCallbackFactory implements ApplicationContextAware {
    /**
     * 配置回调器
     * key -> configType 配置类型
     * value -> ConfigCallback 业务自定义配置回调
     */
    private final Map<String, ConfigChangeCallback<?>> callbackMap = new ConcurrentHashMap<>();

    public ConfigChangeCallback<?> getConfigCallback(String type) {
        ConfigChangeCallback<?> callback = callbackMap.get(type);
        if (callback == null) {
            log.debug("[ConfigCallbackFactory] type: {} is not have configChangeCallBack", type);
            return null;
        }

        return callback;
    }

    /**
     * 执行配置变更回调
     *
     * @param changeType  变更类型
     * @param operateType 操作类型
     * @param config      配置传输对象
     * @return 加工后的 ConfigDTO
     */
    public ConfigDTO doCallback(ConfigChangeTypeEnum changeType, OperateTypeEnum operateType, ConfigDTO config) {
        AssertUtils.isNotNull(config, SYSTEM_ERROR, "ConfigCallbackFactor config is null");
        AssertUtils.isNotBlank(config.getType(), SYSTEM_ERROR, "ConfigCallbackFactory config.type is blank");
        AssertUtils.isNotBlank(config.getCustomId(), SYSTEM_ERROR, "ConfigCallbackFactory config.customId is blank");
        ConfigChangeCallback<?> callback = getConfigCallback(config.getType());
        if (callback == null) {
            log.warn("no callback registered for config type: {}", config.getType());
            return config;
        }
        CallbackContext context = new CallbackContext(changeType, operateType);
        return callback.onChange(context, config);
    }

    /**
     * 注册callback
     * @param applicationContext the ApplicationContext object to be used by this object
     * @throws BeansException Spring Bean异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        applicationContext.getBeansOfType(ConfigChangeCallback.class).forEach((k, callback) -> {
            if (callback == null) {
                return;
            }

            Class<?> configClazz = callback.getConfigClass();
            String clazzName = callback.getClass().getName();
            AssertUtils.isNotNull(configClazz, SYSTEM_ERROR,
                    String.format("ConfigChangeCallback.supportClass can not be null, clazz: {%s}", clazzName));
            Config config = configClazz.getAnnotation(Config.class);
            AssertUtils.isNotNull(config, SYSTEM_ERROR,
                    String.format("ConfigChangeCallback.supportClass() need @Config annotation, clazz: {%s}", clazzName));
            callbackMap.put(config.type(), callback);
        });
    }
}
