package com.scorpion.config.test.convert;

import com.alibaba.fastjson.JSON;
import com.scorpion.config.facade.convert.ConfigConverter;
import com.scorpion.config.facade.model.*;
import com.scorpion.config.facade.rpc.ConfigItem;
import com.scorpion.config.impl.dal.ConfigDO;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class ConfigConverterTest {

    // ==================== DTO ↔ DO ====================

    @Test
    public void testDto2Do_normal() {
        ConfigDTO dto = buildConfigDTO();
        ConfigDO doObj = ConfigConverter.dto2Do(dto);

        assertNotNull(doObj);
        assertEquals(dto.getAppName(), doObj.getAppName());
        assertEquals(dto.getType(), doObj.getType());
        assertEquals(dto.getCustomId(), doObj.getCustomId());
        assertEquals(dto.getName(), doObj.getName());
        assertEquals(dto.getStatus(), doObj.getStatus());
        assertEquals(dto.getContent(), doObj.getContent());
        assertEquals(dto.getExtra(), doObj.getExtra());
    }

    @Test
    public void testDto2Do_null() {
        assertNull(ConfigConverter.dto2Do(null));
    }

    @Test
    public void testDto2Do_emptyStrategyConfigs() {
        ConfigDTO dto = new ConfigDTO();
        dto.setId(1L);
        dto.setStrategyConfigs(new ArrayList<>());
        ConfigDO doObj = ConfigConverter.dto2Do(dto);
        assertNull(doObj.getStrategyConfigs());
    }

    @Test
    public void testDo2Dto_normal() {
        ConfigDO doObj = buildConfigDO();
        ConfigDTO dto = ConfigConverter.do2DTO(doObj);

        assertNotNull(dto);
        assertEquals(doObj.getAppName(), dto.getAppName());
        assertEquals(doObj.getType(), dto.getType());
        assertEquals(doObj.getCustomId(), dto.getCustomId());
        assertEquals(doObj.getName(), dto.getName());
        assertEquals(doObj.getStatus(), dto.getStatus());
        assertEquals(doObj.getContent(), dto.getContent());
        assertEquals(doObj.getExtra(), dto.getExtra());
    }

    @Test
    public void testDo2Dto_null() {
        assertNull(ConfigConverter.do2DTO((ConfigDO) null));
    }

    @Test
    public void testDo2Dto_nullId() {
        ConfigDO doObj = new ConfigDO();
        doObj.setId(null);
        ConfigDTO dto = ConfigConverter.do2DTO(doObj);
        assertEquals(0L, dto.getId());
    }

    @Test
    public void testDo2Dto_emptyStrategyConfigs() {
        ConfigDO doObj = new ConfigDO();
        doObj.setId(1L);
        doObj.setStrategyConfigs("[]");
        ConfigDTO dto = ConfigConverter.do2DTO(doObj);
        assertNotNull(dto.getStrategyConfigs());
        assertTrue(dto.getStrategyConfigs().isEmpty());
    }

    @Test
    public void testDo2Dto_nullStrategyConfigs() {
        ConfigDO doObj = new ConfigDO();
        doObj.setId(1L);
        doObj.setStrategyConfigs(null);
        ConfigDTO dto = ConfigConverter.do2DTO(doObj);
        assertNull(dto.getStrategyConfigs());
    }

    // ==================== DO ↔ RPC ====================

    @Test
    public void testDo2RpcModel_normal() {
        ConfigDO doObj = buildConfigDO();
        ConfigItem item = ConfigConverter.do2RpcModel(doObj);

        assertNotNull(item);
        assertEquals(doObj.getId().longValue(), item.getId());
        assertEquals(doObj.getAppName(), item.getAppName());
        assertNotNull(item.getCreateTime());
        assertNotNull(item.getUpdateTime());
    }

    @Test
    public void testDo2RpcModel_null() {
        assertNull(ConfigConverter.do2RpcModel((ConfigDO) null));
    }

    @Test
    public void testRpcModel2Do_normal() {
        ConfigItem item = buildRpcConfigItem();
        ConfigDO doObj = ConfigConverter.rpcModel2Do(item);

        assertNotNull(doObj);
        assertEquals(Long.valueOf(item.getId()), doObj.getId());
        assertEquals(item.getAppName(), doObj.getAppName());
        assertNotNull(doObj.getCreateTime());
        assertNotNull(doObj.getUpdateTime());
    }

    @Test
    public void testRpcModel2Do_null() {
        assertNull(ConfigConverter.rpcModel2Do((ConfigItem) null));
    }

    @Test
    public void testDo2RpcDo_roundTrip() {
        ConfigDO original = buildConfigDO();
        ConfigItem item = ConfigConverter.do2RpcModel(original);
        ConfigDO result = ConfigConverter.rpcModel2Do(item);

        assertEquals(original.getId(), result.getId());
        assertEquals(original.getAppName(), result.getAppName());
        assertEquals(original.getType(), result.getType());
    }

    // ==================== DTO ↔ RPC ====================

    @Test
    public void testDto2RpcModel_normal() {
        ConfigDTO dto = buildConfigDTO();
        ConfigItem item = ConfigConverter.dto2RpcModel(dto);

        assertNotNull(item);
        assertEquals(dto.getId(), item.getId());
        assertEquals(dto.getAppName(), item.getAppName());
        assertNotNull(item.getCreateTime());
        assertNotNull(item.getStrategyConfigs());
        assertTrue(item.getStrategyConfigs().contains("WHITE_LIST"));
    }

    @Test
    public void testDto2RpcModel_null() {
        assertNull(ConfigConverter.dto2RpcModel((ConfigDTO) null));
    }

    @Test
    public void testRpcModel2Dto_normal() {
        ConfigItem item = buildRpcConfigItem();
        ConfigDTO dto = ConfigConverter.rpcModel2Dto(item);

        assertNotNull(dto);
        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getAppName(), dto.getAppName());
    }

    @Test
    public void testRpcModel2Dto_null() {
        assertNull(ConfigConverter.rpcModel2Dto((ConfigItem) null));
    }

    @Test
    public void testDto2RpcDto_roundTrip() {
        ConfigDTO original = buildConfigDTO();
        ConfigItem item = ConfigConverter.dto2RpcModel(original);
        ConfigDTO result = ConfigConverter.rpcModel2Dto(item);

        assertEquals(original.getId(), result.getId());
        assertEquals(original.getAppName(), result.getAppName());
        assertEquals(original.getType(), result.getType());
    }

    // ==================== Batch methods ====================

    @Test
    public void testDto2DoList_normal() {
        List<ConfigDTO> dtos = new ArrayList<>();
        dtos.add(buildConfigDTO());
        dtos.add(buildConfigDTO());
        List<ConfigDO> dos = ConfigConverter.dto2DO(dtos);
        assertEquals(2, dos.size());
    }

    @Test
    public void testDto2DoList_empty() {
        List<ConfigDO> dos = ConfigConverter.dto2DO(new ArrayList<>());
        assertTrue(dos.isEmpty());
    }

    @Test
    public void testDto2DoList_null() {
        List<ConfigDO> dos = ConfigConverter.dto2DO(null);
        assertTrue(dos.isEmpty());
    }

    @Test
    public void testDo2DtoList_normal() {
        List<ConfigDO> dos = new ArrayList<>();
        dos.add(buildConfigDO());
        dos.add(buildConfigDO());
        List<ConfigDTO> dtos = ConfigConverter.do2DTO(dos);
        assertEquals(2, dtos.size());
    }

    @Test
    public void testDo2DtoList_empty() {
        List<ConfigDTO> dtos = ConfigConverter.do2DTO(new ArrayList<>());
        assertTrue(dtos.isEmpty());
    }

    @Test
    public void testDo2DtoList_null() {
        List<ConfigDTO> dtos = ConfigConverter.do2DTO((List<ConfigDO>) null);
        assertTrue(dtos.isEmpty());
    }

    // ==================== Build helpers ====================

    private ConfigDTO buildConfigDTO() {
        ConfigDTO dto = new ConfigDTO();
        dto.setId(1L);
        dto.setCreateTime(new Date());
        dto.setUpdateTime(new Date());
        dto.setAppName("test-app");
        dto.setType("activity_config");
        dto.setCustomId("act_001");
        dto.setName("Test Activity");
        dto.setStatus("ONLINE");
        dto.setContent("{\"name\":\"活动A\"}");
        dto.setExtra("{}");

        StrategyDTO strategy = new StrategyDTO();
        strategy.setStrategyType("WHITE_LIST");
        strategy.setWhiteList("user1,user2");

        StrategyConfigDTO strategyConfig = new StrategyConfigDTO();
        strategyConfig.setStrategyList(new ArrayList<>());
        strategyConfig.getStrategyList().add(strategy);
        strategyConfig.setContent("{\"key\":\"value\"}");

        dto.setStrategyConfigs(new ArrayList<>());
        dto.getStrategyConfigs().add(strategyConfig);
        return dto;
    }

    private ConfigDO buildConfigDO() {
        ConfigDO doObj = new ConfigDO();
        doObj.setId(1L);
        doObj.setCreateTime(new Date());
        doObj.setUpdateTime(new Date());
        doObj.setAppName("test-app");
        doObj.setType("activity_config");
        doObj.setCustomId("act_001");
        doObj.setName("Test Activity");
        doObj.setStatus("ONLINE");
        doObj.setContent("{\"name\":\"活动A\"}");
        doObj.setExtra("{}");

        StrategyDTO strategy = new StrategyDTO();
        strategy.setStrategyType("WHITE_LIST");
        strategy.setWhiteList("user1,user2");

        StrategyConfigDTO strategyConfig = new StrategyConfigDTO();
        strategyConfig.setStrategyList(new ArrayList<>());
        strategyConfig.getStrategyList().add(strategy);
        strategyConfig.setContent("{\"key\":\"value\"}");

        List<StrategyConfigDTO> list = new ArrayList<>();
        list.add(strategyConfig);
        doObj.setStrategyConfigs(JSON.toJSONString(list));
        return doObj;
    }

    private ConfigItem buildRpcConfigItem() {
        ConfigItem item = new ConfigItem();
        item.setId(1L);
        item.setCreateTime("2026-04-18 10:00:00");
        item.setUpdateTime("2026-04-18 11:00:00");
        item.setAppName("test-app");
        item.setType("activity_config");
        item.setCustomId("act_001");
        item.setName("Test Activity");
        item.setStatus("ONLINE");
        item.setContent("{\"name\":\"活动A\"}");
        item.setExtra("{}");
        item.setStrategyConfigs("[]");
        return item;
    }
}
