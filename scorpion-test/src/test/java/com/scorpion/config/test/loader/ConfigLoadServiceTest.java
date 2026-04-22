package com.scorpion.config.test.loader;

import com.scorpion.config.api.provider.ConfigProvider;
import com.scorpion.config.facade.model.AppAllConfigDTO;
import com.scorpion.config.facade.model.ConfigDTO;
import com.scorpion.config.impl.loader.ConfigLoadService;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ConfigLoadServiceTest {

    @Test
    public void testLoadAndBackup_firstProviderSucceeds() {
        List<ConfigProvider> providers = new ArrayList<>();
        providers.add(new MockConfigProvider("provider1", 0, buildAppAllConfig(1)));
        providers.add(new MockConfigProvider("provider2", 1, null));

        ConfigLoadService service = new ConfigLoadService(providers);
        AppAllConfigDTO result = service.loadAndBackup(0, 0);

        assertNotNull(result);
        assertEquals(1, result.getVersion());
    }

    @Test
    public void testLoadAndBackup_firstProviderFallsBack() {
        List<ConfigProvider> providers = new ArrayList<>();
        providers.add(new MockConfigProvider("provider1", 0, null));
        providers.add(new MockConfigProvider("provider2", 1, buildAppAllConfig(2)));

        ConfigLoadService service = new ConfigLoadService(providers);
        AppAllConfigDTO result = service.loadAndBackup(0, 0);

        assertNotNull(result);
        assertEquals(2, result.getVersion());
    }

    @Test
    public void testLoadAndBackup_firstProviderThrows() {
        List<ConfigProvider> providers = new ArrayList<>();
        providers.add(new FailingConfigProvider("provider1", 0));
        providers.add(new MockConfigProvider("provider2", 1, buildAppAllConfig(3)));

        ConfigLoadService service = new ConfigLoadService(providers);
        AppAllConfigDTO result = service.loadAndBackup(0, 0);

        assertNotNull(result);
        assertEquals(3, result.getVersion());
    }

    @Test
    public void testLoadAndBackup_allFail() {
        List<ConfigProvider> providers = new ArrayList<>();
        providers.add(new FailingConfigProvider("provider1", 0));
        providers.add(new MockConfigProvider("provider2", 1, null));

        ConfigLoadService service = new ConfigLoadService(providers);
        AppAllConfigDTO result = service.loadAndBackup(0, 0);

        assertNull(result);
    }

    @Test
    public void testLoadAndBackup_allReturnNull() {
        List<ConfigProvider> providers = new ArrayList<>();
        providers.add(new MockConfigProvider("provider1", 0, null));
        providers.add(new MockConfigProvider("provider2", 1, null));

        ConfigLoadService service = new ConfigLoadService(providers);
        AppAllConfigDTO result = service.loadAndBackup(0, 0);

        assertNull(result);
    }

    private AppAllConfigDTO buildAppAllConfig(int version) {
        AppAllConfigDTO dto = new AppAllConfigDTO();
        dto.setVersion(version);
        dto.setConfigList(new ArrayList<>());
        ConfigDTO config = new ConfigDTO();
        config.setId(1L);
        config.setAppName("test-app");
        config.setType("test_type");
        config.setCustomId("test_001");
        config.setName("Test Config");
        config.setStatus("ONLINE");
        dto.getConfigList().add(config);
        return dto;
    }

    private static class MockConfigProvider implements ConfigProvider {
        private final String name;
        private final int order;
        private final AppAllConfigDTO result;

        MockConfigProvider(String name, int order, AppAllConfigDTO result) {
            this.name = name;
            this.order = order;
            this.result = result;
        }

        @Override
        public int order() { return order; }

        @Override
        public AppAllConfigDTO loadAppAllConfigs(long currentVersion) { return result; }
    }

    private static class FailingConfigProvider implements ConfigProvider {
        private final String name;
        private final int order;

        FailingConfigProvider(String name, int order) {
            this.name = name;
            this.order = order;
        }

        @Override
        public int order() { return order; }

        @Override
        public AppAllConfigDTO loadAppAllConfigs(long currentVersion) {
            throw new RuntimeException("Connection failed: " + name);
        }
    }
}
