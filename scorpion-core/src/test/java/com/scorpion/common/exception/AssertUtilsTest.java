package com.scorpion.common.exception;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class AssertUtilsTest {

    private static final ResultCode CODE = CommonResultCodeEnum.PARAMETER_ILLEGAL;

    // --- isTrue / isFalse ---

    @Test
    public void test_isTrue_pass() {
        AssertUtils.isTrue(true, CODE);
    }

    @Test(expected = CommonException.class)
    public void test_isTrue_fail() {
        AssertUtils.isTrue(false, CODE);
    }

    @Test
    public void test_isTrue_with_message_pass() {
        AssertUtils.isTrue(true, CODE, "msg");
    }

    @Test
    public void test_isTrue_with_message_fail() {
        try {
            AssertUtils.isTrue(false, CODE, "custom msg");
            fail();
        } catch (CommonException e) {
            assertEquals("custom msg", e.getMessage());
        }
    }

    @Test
    public void test_isFalse_pass() {
        AssertUtils.isFalse(false, CODE);
    }

    @Test(expected = CommonException.class)
    public void test_isFalse_fail() {
        AssertUtils.isFalse(true, CODE);
    }

    // --- isNull / isNotNull ---

    @Test
    public void test_isNull_pass() {
        AssertUtils.isNull(null, CODE);
    }

    @Test(expected = CommonException.class)
    public void test_isNull_fail() {
        AssertUtils.isNull("obj", CODE);
    }

    @Test
    public void test_isNotNull_pass() {
        AssertUtils.isNotNull("obj", CODE);
    }

    @Test(expected = CommonException.class)
    public void test_isNotNull_fail() {
        AssertUtils.isNotNull(null, CODE);
    }

    // --- isBlank / isNotBlank ---

    @Test
    public void test_isBlank_pass() {
        AssertUtils.isBlank("", CODE);
        AssertUtils.isBlank("  ", CODE);
        AssertUtils.isBlank(null, CODE);
    }

    @Test(expected = CommonException.class)
    public void test_isBlank_fail() {
        AssertUtils.isBlank("hello", CODE);
    }

    @Test
    public void test_isNotBlank_pass() {
        AssertUtils.isNotBlank("hello", CODE);
    }

    @Test(expected = CommonException.class)
    public void test_isNotBlank_fail() {
        AssertUtils.isNotBlank("", CODE);
    }

    // --- isNotEmpty ---

    @Test
    public void test_isNotEmpty_array_pass() {
        AssertUtils.isNotEmpty(new Object[]{1}, CODE);
    }

    @Test(expected = CommonException.class)
    public void test_isNotEmpty_array_fail() {
        AssertUtils.isNotEmpty(new Object[0], CODE);
    }

    @Test
    public void test_isNotEmpty_collection_pass() {
        AssertUtils.isNotEmpty(Arrays.asList(1), CODE);
    }

    @Test(expected = CommonException.class)
    public void test_isNotEmpty_collection_fail() {
        AssertUtils.isNotEmpty(new ArrayList<>(), CODE);
    }

    @Test
    public void test_isNotEmpty_map_pass() {
        Map<String, String> map = new HashMap<>();
        map.put("k", "v");
        AssertUtils.isNotEmpty(map, CODE);
    }

    @Test(expected = CommonException.class)
    public void test_isNotEmpty_map_fail() {
        AssertUtils.isNotEmpty(new HashMap<>(), CODE);
    }

    // --- isEquals / isNotEquals ---

    @Test
    public void test_isEquals_pass() {
        AssertUtils.isEquals("a", "a", CODE);
        AssertUtils.isEquals(null, null, CODE);
    }

    @Test(expected = CommonException.class)
    public void test_isEquals_fail() {
        AssertUtils.isEquals("a", "b", CODE);
    }

    @Test
    public void test_isNotEquals_pass() {
        AssertUtils.isNotEquals("a", "b", CODE);
    }

    @Test(expected = CommonException.class)
    public void test_isNotEquals_fail() {
        AssertUtils.isNotEquals("a", "a", CODE);
    }

    // --- resultCode null -> NPE ---

    @Test(expected = NullPointerException.class)
    public void test_isTrue_null_resultCode_throws_npe() {
        AssertUtils.isTrue(true, null);
    }

    @Test(expected = NullPointerException.class)
    public void test_isNotNull_null_resultCode_throws_npe() {
        AssertUtils.isNotNull("obj", null);
    }

    @Test(expected = NullPointerException.class)
    public void test_isNotBlank_null_resultCode_throws_npe() {
        AssertUtils.isNotBlank("text", null);
    }
}
