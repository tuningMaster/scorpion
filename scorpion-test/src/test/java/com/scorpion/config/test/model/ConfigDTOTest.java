package com.scorpion.config.test.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.scorpion.config.facade.model.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ConfigDTOTest {

    @Test
    public void testConfigDtoStrategyConfigsRoundTrip() {
        // Build a ConfigDTO with strategy configs
        ConfigDTO dto = buildConfigDTO();

        // Serialize strategyConfigs to JSON (as ConfigConverter.dto2Do would)
        String json = JSON.toJSONString(dto.getStrategyConfigs());
        assertNotNull(json);
        assertTrue(json.contains("WHITE_LIST"));

        // Deserialize back (as ConfigConverter.do2Dto would)
        List<StrategyConfigDTO> parsed = JSON.parseObject(json,
                new TypeReference<List<StrategyConfigDTO>>() {});
        assertNotNull(parsed);
        assertEquals(1, parsed.size());

        StrategyConfigDTO strategyConfig = parsed.get(0);
        assertEquals(1, strategyConfig.getStrategyList().size());
        assertEquals("WHITE_LIST", strategyConfig.getStrategyList().get(0).getStrategyType());
        assertEquals("user1,user2", strategyConfig.getStrategyList().get(0).getWhiteList());
        assertEquals("whiteListConfig", strategyConfig.getContent());
    }

    @Test
    public void testConfigDtoFullSerialization() {
        ConfigDTO dto = buildConfigDTO();

        String json = JSON.toJSONString(dto);
        assertTrue(json.contains("test-app"));
        assertTrue(json.contains("activity_config"));
        assertTrue(json.contains("act_001"));
        assertTrue(json.contains("WHITE_LIST"));
    }

    @Test
    public void testConfigDtoFullDeserialization() {
        String json = "{\"id\":1,\"appName\":\"test-app\",\"type\":\"activity_config\","
                + "\"customId\":\"act_001\",\"name\":\"Test\",\"status\":\"ONLINE\","
                + "\"content\":\"{}\",\"extra\":null,\"strategyConfigs\":[]}";

        ConfigDTO dto = JSON.parseObject(json, ConfigDTO.class);
        assertEquals(1L, dto.getId());
        assertEquals("test-app", dto.getAppName());
        assertEquals("activity_config", dto.getType());
        assertEquals("act_001", dto.getCustomId());
        assertEquals("ONLINE", dto.getStatus());
        assertNotNull(dto.getStrategyConfigs());
        assertTrue(dto.getStrategyConfigs().isEmpty());
    }

    @Test
    public void testConfigDtoWithMultipleStrategyConfigs() {
        ConfigDTO dto = buildConfigDTO();

        // Add a percent strategy config
        StrategyDTO percentStrategy = new StrategyDTO();
        percentStrategy.setStrategyType("PERCENT");
        percentStrategy.setPercent(10);

        StrategyConfigDTO percentConfig = new StrategyConfigDTO();
        percentConfig.setStrategyList(new ArrayList<>());
        percentConfig.getStrategyList().add(percentStrategy);
        percentConfig.setContent("{\"name\":\"percent\"}");
        dto.getStrategyConfigs().add(percentConfig);

        // Serialize and deserialize
        String json = JSON.toJSONString(dto.getStrategyConfigs());
        List<StrategyConfigDTO> parsed = JSON.parseObject(json,
                new TypeReference<List<StrategyConfigDTO>>() {});

        assertEquals(2, parsed.size());
        assertEquals("WHITE_LIST", parsed.get(0).getStrategyList().get(0).getStrategyType());
        assertEquals("PERCENT", parsed.get(1).getStrategyList().get(0).getStrategyType());
        assertEquals(Integer.valueOf(10), parsed.get(1).getStrategyList().get(0).getPercent());
    }

    @Test
    public void testConfigDtoToStringExcludesContent() {
        ConfigDTO dto = buildConfigDTO();
        String str = dto.toString();
        assertFalse(str.contains("large content"));
        assertFalse(str.contains("whiteListConfig"));
    }

    private ConfigDTO buildConfigDTO() {
        ConfigDTO dto = new ConfigDTO();
        dto.setId(1L);
        dto.setAppName("test-app");
        dto.setType("activity_config");
        dto.setCustomId("act_001");
        dto.setName("Test Activity");
        dto.setStatus("ONLINE");
        dto.setContent("{\"name\":\"large content\"}");
        dto.setExtra("{}");

        StrategyDTO strategy = new StrategyDTO();
        strategy.setStrategyType("WHITE_LIST");
        strategy.setWhiteList("user1,user2");

        StrategyConfigDTO strategyConfig = new StrategyConfigDTO();
        strategyConfig.setStrategyList(new ArrayList<>());
        strategyConfig.getStrategyList().add(strategy);
        strategyConfig.setContent("whiteListConfig");

        dto.setStrategyConfigs(new ArrayList<>());
        dto.getStrategyConfigs().add(strategyConfig);
        return dto;
    }
}
