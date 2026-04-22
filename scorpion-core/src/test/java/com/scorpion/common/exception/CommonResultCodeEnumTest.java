package com.scorpion.common.exception;

import org.junit.Test;

import static org.junit.Assert.*;

public class CommonResultCodeEnumTest {

    @Test
    public void test_success_code() {
        CommonResultCodeEnum code = CommonResultCodeEnum.SUCCESS;
        assertEquals(0, code.getCode());
        assertEquals("成功", code.getMessage());
        assertFalse(code.isRetryable());
    }

    @Test
    public void test_system_error_code() {
        CommonResultCodeEnum code = CommonResultCodeEnum.SYSTEM_ERROR;
        assertEquals(10000, code.getCode());
        assertEquals("系统异常", code.getMessage());
        assertTrue(code.isRetryable());
    }

    @Test
    public void test_parameter_illegal() {
        CommonResultCodeEnum code = CommonResultCodeEnum.PARAMETER_ILLEGAL;
        assertEquals(10001, code.getCode());
        assertEquals("参数非法", code.getMessage());
        assertFalse(code.isRetryable());
    }

    @Test
    public void test_all_enum_values_exist() {
        CommonResultCodeEnum[] values = CommonResultCodeEnum.values();
        assertEquals(11, values.length);
    }

    @Test
    public void test_codeDescription_returns_name() {
        for (CommonResultCodeEnum e : CommonResultCodeEnum.values()) {
            assertEquals(e.name(), e.getCodeDescription());
        }
    }

    @Test
    public void test_findByCode() {
        assertEquals(CommonResultCodeEnum.SUCCESS, CommonResultCodeEnum.findByCode(0));
        assertEquals(CommonResultCodeEnum.SYSTEM_ERROR, CommonResultCodeEnum.findByCode(10000));
        assertEquals(CommonResultCodeEnum.AUTH_FAIL, CommonResultCodeEnum.findByCode(10009));
        assertNull(CommonResultCodeEnum.findByCode(99999));
    }
}
