package com.scorpion.config.test.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.scorpion.config.facade.model.ConfigDTO;
import com.scorpion.config.facade.model.StrategyConfigDTO;
import com.scorpion.config.facade.model.StrategyDTO;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class StrategyDTOTest {

    @Test
    public void testWhiteListSerialization() {
        StrategyDTO dto = new StrategyDTO();
        dto.setStrategyType("WHITE_LIST");
        dto.setWhiteList("user1,user2,user3");

        String json = JSON.toJSONString(dto);
        assertTrue(json.contains("WHITE_LIST"));
        assertTrue(json.contains("user1,user2,user3"));

        StrategyDTO parsed = JSON.parseObject(json, StrategyDTO.class);
        assertEquals("WHITE_LIST", parsed.getStrategyType());
        assertEquals("user1,user2,user3", parsed.getWhiteList());
        assertNull(parsed.getPercent());
    }

    @Test
    public void testPercentSerialization() {
        StrategyDTO dto = new StrategyDTO();
        dto.setStrategyType("PERCENT");
        dto.setPercent(30);

        String json = JSON.toJSONString(dto);
        assertTrue(json.contains("PERCENT"));
        assertTrue(json.contains("30"));

        StrategyDTO parsed = JSON.parseObject(json, StrategyDTO.class);
        assertEquals("PERCENT", parsed.getStrategyType());
        assertEquals(Integer.valueOf(30), parsed.getPercent());
    }

    @Test
    public void testBetaSerialization() {
        StrategyDTO dto = new StrategyDTO();
        dto.setStrategyType("BETA");

        String json = JSON.toJSONString(dto);
        StrategyDTO parsed = JSON.parseObject(json, StrategyDTO.class);
        assertEquals("BETA", parsed.getStrategyType());
    }

    @Test
    public void testStrategyConfigDTOSerialization() {
        List<StrategyDTO> strategies = new ArrayList<>();

        StrategyDTO whiteList = new StrategyDTO();
        whiteList.setStrategyType("WHITE_LIST");
        whiteList.setWhiteList("user1");
        strategies.add(whiteList);

        StrategyDTO percent = new StrategyDTO();
        percent.setStrategyType("PERCENT");
        percent.setPercent(50);
        strategies.add(percent);

        StrategyConfigDTO configDTO = new StrategyConfigDTO();
        configDTO.setStrategyList(strategies);
        configDTO.setContent("{\"name\":\"灰度配置\"}");

        String json = JSON.toJSONString(configDTO);
        assertTrue(json.contains("WHITE_LIST"));
        assertTrue(json.contains("PERCENT"));
        assertTrue(json.contains("灰度配置"));

        StrategyConfigDTO parsed = JSON.parseObject(json, StrategyConfigDTO.class);
        assertEquals(2, parsed.getStrategyList().size());
        assertEquals("WHITE_LIST", parsed.getStrategyList().get(0).getStrategyType());
        assertEquals("PERCENT", parsed.getStrategyList().get(1).getStrategyType());
    }

    @Test
    public void testStrategyConfigDTOListSerialization() {
        // Test the full list serialization as used in ConfigConverter
        List<StrategyConfigDTO> list = new ArrayList<>();

        StrategyConfigDTO item1 = new StrategyConfigDTO();
        StrategyDTO s1 = new StrategyDTO();
        s1.setStrategyType("WHITE_LIST");
        s1.setWhiteList("u1,u2");
        item1.setStrategyList(new ArrayList<>());
        item1.getStrategyList().add(s1);
        item1.setContent("config1");
        list.add(item1);

        String json = JSON.toJSONString(list);
        List<StrategyConfigDTO> parsed = JSON.parseObject(json,
                new TypeReference<List<StrategyConfigDTO>>() {});

        assertNotNull(parsed);
        assertEquals(1, parsed.size());
        assertEquals("WHITE_LIST", parsed.get(0).getStrategyList().get(0).getStrategyType());
        assertEquals("u1,u2", parsed.get(0).getStrategyList().get(0).getWhiteList());
        assertEquals("config1", parsed.get(0).getContent());
    }
}
