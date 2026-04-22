package com.scorpion.config.test.memory;

import com.scorpion.config.impl.memory.MemoryConfigIndex;
import org.junit.Test;

import static org.junit.Assert.*;

public class MemoryConfigIndexTest {

    @Test
    public void testCompareTo_sameKey() {
        MemoryConfigIndex a = MemoryConfigIndex.builder()
                .configType("type1").indexType("city").indexKey("shanghai").configId("id1").build();
        MemoryConfigIndex b = MemoryConfigIndex.builder()
                .configType("type1").indexType("city").indexKey("shanghai").configId("id1").build();
        assertEquals(0, a.compareTo(b));
    }

    @Test
    public void testCompareTo_configTypeFirst() {
        MemoryConfigIndex a = MemoryConfigIndex.builder()
                .configType("type1").build();
        MemoryConfigIndex b = MemoryConfigIndex.builder()
                .configType("type2").build();
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }

    @Test
    public void testCompareTo_indexTypeSecond() {
        MemoryConfigIndex a = MemoryConfigIndex.builder()
                .configType("type1").indexType("city").build();
        MemoryConfigIndex b = MemoryConfigIndex.builder()
                .configType("type1").indexType("region").build();
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    public void testCompareTo_indexKeyThird() {
        MemoryConfigIndex a = MemoryConfigIndex.builder()
                .configType("type1").indexType("city").indexKey("beijing").build();
        MemoryConfigIndex b = MemoryConfigIndex.builder()
                .configType("type1").indexType("city").indexKey("shanghai").build();
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    public void testCompareTo_configIdLast() {
        MemoryConfigIndex a = MemoryConfigIndex.builder()
                .configType("type1").indexType(null).indexKey(null).configId("id1").build();
        MemoryConfigIndex b = MemoryConfigIndex.builder()
                .configType("type1").indexType(null).indexKey(null).configId("id2").build();
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    public void testCompareTo_nullFirst() {
        MemoryConfigIndex nullConfigType = MemoryConfigIndex.builder()
                .configType(null).build();
        MemoryConfigIndex hasConfigType = MemoryConfigIndex.builder()
                .configType("type1").build();
        assertTrue(nullConfigType.compareTo(hasConfigType) < 0);
    }

    @Test
    public void testCompareTo_bothNull() {
        MemoryConfigIndex a = MemoryConfigIndex.builder().build();
        MemoryConfigIndex b = MemoryConfigIndex.builder().build();
        assertEquals(0, a.compareTo(b));
    }

    @Test
    public void testCompareTo_nullVsNonNull() {
        MemoryConfigIndex a = MemoryConfigIndex.builder()
                .configType("type1").indexType(null).indexKey(null).configId(null).build();
        MemoryConfigIndex b = MemoryConfigIndex.builder()
                .configType("type1").indexType("city").indexKey(null).configId(null).build();
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    public void testBuilder_allFields() {
        MemoryConfigIndex index = MemoryConfigIndex.builder()
                .configType("activity_config")
                .indexType("city")
                .indexKey("shanghai")
                .configId("act_001")
                .build();
        assertEquals("activity_config", index.getConfigType());
        assertEquals("city", index.getIndexType());
        assertEquals("shanghai", index.getIndexKey());
        assertEquals("act_001", index.getConfigId());
    }
}
