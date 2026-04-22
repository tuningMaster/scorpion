package com.scorpion.config.api.provider;

import com.scorpion.config.facade.convert.ConfigConverter;
import com.scorpion.config.facade.model.AppAllConfigDTO;
import com.scorpion.config.impl.dal.ConfigRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DatabaseConfigProvier implements ConfigProvider{
    /**
     * DB配置reposiroty
     */
    private ConfigRepository configRepository;
    @Override
    public AppAllConfigDTO loadAppAllConfigs(long currentVersion) {
        AppAllConfigDTO appAllConfigDTO = new AppAllConfigDTO();
        appAllConfigDTO.setConfigList(ConfigConverter.do2DTO(configRepository.listAllOnlineConfig()));
        appAllConfigDTO.setVersion(currentVersion + 1);
        return appAllConfigDTO;
    }
}
