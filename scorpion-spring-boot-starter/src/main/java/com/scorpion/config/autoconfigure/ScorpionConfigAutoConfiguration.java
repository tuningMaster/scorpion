package com.scorpion.config.autoconfigure;

import com.scorpion.config.api.provider.ConfigProvider;
import com.scorpion.config.api.provider.DatabaseConfigProvider;
import com.scorpion.config.autoconfigure.properties.ScorpionConfigProperties;
import com.scorpion.config.core.ConfigEngine;
import com.scorpion.config.impl.dal.ConfigRepository;
import com.scorpion.config.impl.loader.JdbcTemplateConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * scorpion-config 自动配置
 * scorpion.config.enabled=false 时不初始化
 */
@Configuration
@EnableConfigurationProperties(ScorpionConfigProperties.class)
@ConditionalOnProperty(prefix = "scorpion.config", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ScorpionConfigAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ScorpionConfigAutoConfiguration.class);

    @Autowired
    private ScorpionConfigProperties properties;

    @Autowired(required = false)
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private List<ConfigProvider> customProviders;

    @PostConstruct
    public void init() {
        if (properties.getAppName() == null || properties.getAppName().isEmpty()) {
            throw new IllegalStateException("scorpion.config.app-name must be configured");
        }
        if (properties.getTableName() == null || properties.getTableName().isEmpty()) {
            throw new IllegalStateException("scorpion.config.table-name must be configured");
        }

        List<ConfigProvider> providers = new ArrayList<>();

        // 优先使用自定义 provider
        if (customProviders != null && !customProviders.isEmpty()) {
            providers.addAll(customProviders);
        }

        // 如果没有自定义 provider 且有 jdbcTemplate，创建默认的数据库 provider
        if (providers.isEmpty() && jdbcTemplate != null) {
            ConfigRepository repository = new JdbcTemplateConfigRepository(jdbcTemplate, properties.getTableName());
            providers.add(new DatabaseConfigProvider(repository, properties.getAppName()));
        }

        if (providers.isEmpty()) {
            log.warn("No ConfigProvider configured. ConfigEngine will not be able to load configs.");
            return;
        }

        ConfigRepository repository = new JdbcTemplateConfigRepository(jdbcTemplate, properties.getTableName());

        ConfigEngine engine = ConfigEngine.getInstance();
        engine.setConfigRepository(repository);
        engine.setConfigProviderList(providers);
        engine.setConfigCallbackFactory(new com.scorpion.config.api.callback.ConfigCallbackFactory());
        engine.init(providers, properties.getReloadInterval());
        log.info("Scorpion config initialized: app={}, table={}, reloadInterval={}ms",
                properties.getAppName(), properties.getTableName(), properties.getReloadInterval());
    }

    @PreDestroy
    public void destroy() {
        ConfigEngine.shutdown();
    }
}
