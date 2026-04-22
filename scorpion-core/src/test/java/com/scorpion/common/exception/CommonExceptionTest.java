package com.scorpion.common.exception;

import org.junit.Test;

import static org.junit.Assert.*;

public class CommonExceptionTest {

    @Test
    public void test_constructor_with_resultCode() {
        CommonException ex = new CommonException(CommonResultCodeEnum.PARAMETER_ILLEGAL);
        assertEquals(10001, ex.getCode());
        assertEquals("参数非法", ex.getMessage());
        assertFalse(ex.isRetryable());
        assertNull(ex.getCause());
    }

    @Test
    public void test_constructor_with_string_message() {
        CommonException ex = new CommonException("custom error");
        assertEquals(CommonResultCodeEnum.SYSTEM_ERROR.getCode(), ex.getCode());
        assertEquals("custom error", ex.getMessage());
        assertTrue(ex.isRetryable());
    }

    @Test
    public void test_constructor_with_resultCode_and_message() {
        CommonException ex = new CommonException(CommonResultCodeEnum.INTEGRATION_ERROR, "downstream timeout");
        assertEquals(10002, ex.getCode());
        assertEquals("downstream timeout", ex.getMessage());
        assertTrue(ex.isRetryable());
    }

    @Test
    public void test_constructor_with_resultCode_message_cause() {
        Throwable cause = new RuntimeException("root cause");
        CommonException ex = new CommonException(CommonResultCodeEnum.STORE_ERROR, "db error", cause);
        assertEquals(10003, ex.getCode());
        assertEquals("db error", ex.getMessage());
        assertTrue(ex.isRetryable());
        assertSame(cause, ex.getCause());
    }

    @Test
    public void test_constructor_full_params() {
        Throwable cause = new RuntimeException("root");
        CommonException ex = new CommonException(999, "detail", false, "custom-desc", cause);
        assertEquals(999, ex.getCode());
        assertEquals("detail", ex.getMessage());
        assertFalse(ex.isRetryable());
        assertEquals("custom-desc", ex.getCodeDescription());
        assertSame(cause, ex.getCause());
    }
}
