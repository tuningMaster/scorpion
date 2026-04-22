package com.scorpion.config.test.enums;

import com.scorpion.config.facade.enums.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class EnumTest {

    // ==================== ConfigStatusEnum ====================

    @Test
    public void testConfigStatusEnum_values() {
        assertEquals(4, ConfigStatusEnum.values().length);
        assertNotNull(ConfigStatusEnum.valueOf("INIT"));
        assertNotNull(ConfigStatusEnum.valueOf("ONLINE"));
        assertNotNull(ConfigStatusEnum.valueOf("OFFLINE"));
        assertNotNull(ConfigStatusEnum.valueOf("DELETE"));
    }

    // ==================== StrategyTypeEnum ====================

    @Test
    public void testStrategyTypeEnum_findByName_valid() {
        assertEquals(StrategyTypeEnum.WHITE_LIST, StrategyTypeEnum.findByName("WHITE_LIST"));
        assertEquals(StrategyTypeEnum.PERCENT, StrategyTypeEnum.findByName("PERCENT"));
        assertEquals(StrategyTypeEnum.BETA, StrategyTypeEnum.findByName("BETA"));
    }

    @Test
    public void testStrategyTypeEnum_findByName_invalid() {
        assertNull(StrategyTypeEnum.findByName("UNKNOWN"));
        assertNull(StrategyTypeEnum.findByName(""));
        assertNull(StrategyTypeEnum.findByName(null));
    }

    @Test
    public void testStrategyTypeEnum_values() {
        assertEquals(3, StrategyTypeEnum.values().length);
        assertNotNull(StrategyTypeEnum.valueOf("WHITE_LIST"));
        assertNotNull(StrategyTypeEnum.valueOf("PERCENT"));
        assertNotNull(StrategyTypeEnum.valueOf("BETA"));
    }

    // ==================== ConfigChangeTypeEnum ====================

    @Test
    public void testConfigChangeTypeEnum_findByName_valid() {
        assertEquals(ConfigChangeTypeEnum.WHITE_LIST, ConfigChangeTypeEnum.findByName("WHITE_LIST"));
        assertEquals(ConfigChangeTypeEnum.FREEDOM, ConfigChangeTypeEnum.findByName("FREEDOM"));
        assertEquals(ConfigChangeTypeEnum.ROLLBACK, ConfigChangeTypeEnum.findByName("ROLLBACK"));
    }

    @Test
    public void testConfigChangeTypeEnum_findByName_invalid() {
        assertNull(ConfigChangeTypeEnum.findByName("UNKNOWN"));
        assertNull(ConfigChangeTypeEnum.findByName(null));
    }

    // ==================== OperateTypeEnum ====================

    @Test
    public void testOperateTypeEnum_findByName_valid() {
        assertEquals(OperateTypeEnum.CREATE, OperateTypeEnum.findByName("CREATE"));
        assertEquals(OperateTypeEnum.UPDATE, OperateTypeEnum.findByName("UPDATE"));
    }

    @Test
    public void testOperateTypeEnum_findByName_invalid() {
        assertNull(OperateTypeEnum.findByName("UNKNOWN"));
        assertNull(OperateTypeEnum.findByName(null));
    }
}
