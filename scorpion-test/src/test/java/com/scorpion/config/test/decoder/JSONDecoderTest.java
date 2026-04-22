package com.scorpion.config.test.decoder;

import com.scorpion.config.api.decoder.JSONDecoder;
import org.junit.Test;

import static org.junit.Assert.*;

public class JSONDecoderTest {

    @Test
    public void testDecode_normal() {
        JSONDecoder<TestConfig> decoder = new JSONDecoder<>("id");
        String content = "{\"name\":\"test\",\"value\":42}";
        TestConfig result = decoder.decode("config_001", content, TestConfig.class);

        assertNotNull(result);
        assertEquals("test", result.getName());
        assertEquals(Integer.valueOf(42), result.getValue());
        assertEquals("config_001", result.getId());
    }

    @Test
    public void testDecode_nullContent() {
        JSONDecoder<TestConfig> decoder = new JSONDecoder<>("id");
        assertNull(decoder.decode("config_001", null, TestConfig.class));
    }

    @Test
    public void testDecode_emptyContent() {
        JSONDecoder<TestConfig> decoder = new JSONDecoder<>("id");
        assertNull(decoder.decode("config_001", "", TestConfig.class));
    }

    @Test
    public void testDecode_blankContent() {
        JSONDecoder<TestConfig> decoder = new JSONDecoder<>("id");
        assertNull(decoder.decode("config_001", "  ", TestConfig.class));
    }

    @Test
    public void testDecode_noIdField() {
        JSONDecoder<TestConfig> decoder = new JSONDecoder<>(null);
        TestConfig result = decoder.decode("config_001", "{\"name\":\"test\"}", TestConfig.class);

        assertNotNull(result);
        assertEquals("test", result.getName());
        assertNull(result.getId());
    }

    @Test
    public void testDecode_invalidJson() {
        JSONDecoder<TestConfig> decoder = new JSONDecoder<>("id");
        try {
            decoder.decode("config_001", "{invalid json}", TestConfig.class);
            fail("Should throw exception for invalid JSON");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testDecode_nullId() {
        JSONDecoder<TestConfig> decoder = new JSONDecoder<>("id");
        TestConfig result = decoder.decode(null, "{\"name\":\"test\"}", TestConfig.class);

        assertNotNull(result);
        assertEquals("test", result.getName());
    }

    public static class TestConfig {
        private String id;
        private String name;
        private Integer value;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }
    }
}
