package com.scorpion.config.api.provider;

import com.scorpion.config.facade.convert.ConfigConverter;
import com.scorpion.config.facade.model.AppAllConfigDTO;
import com.scorpion.config.facade.model.ConfigDTO;
import com.scorpion.config.impl.dal.ConfigDO;
import com.scorpion.config.impl.dal.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于数据库的配置数据源实现
 */
public class DatabaseConfigProvider implements ConfigProvider {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfigProvider.class);

    private final ConfigRepository configRepository;
    private final String appName;

    public DatabaseConfigProvider(ConfigRepository configRepository, String appName) {
        this.configRepository = configRepository;
        this.appName = appName;
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public AppAllConfigDTO loadAppAllConfigs(long currentVersion) {
        List<ConfigDO> configDOs = configRepository.listAllOnlineConfig();
        if (configDOs == null || configDOs.isEmpty()) {
            return null;
        }
        List<ConfigDTO> configDTOs = new ArrayList<>();
        for (ConfigDO configDO : configDOs) {
            ConfigDTO dto = ConfigConverter.do2DTO(configDO);
            if (dto != null) {
                configDTOs.add(dto);
            }
        }
        AppAllConfigDTO result = new AppAllConfigDTO();
        result.setVersion(currentVersion + 1);
        result.setConfigList(configDTOs);
        log.info("DatabaseConfigProvider loaded {} configs for app {}", configDTOs.size(), appName);
        return result;
    }
}
