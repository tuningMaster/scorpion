package com.scorpion.common.exception;

import org.junit.Test;

import static org.junit.Assert.*;

public class SubExceptionTest {

    // --- ParamsInvalidException ---

    @Test
    public void test_paramsInvalid_with_string_message() {
        ParamsInvalidException ex = new ParamsInvalidException("name is empty");
        assertEquals(CommonResultCodeEnum.PARAMETER_ILLEGAL.getCode(), ex.getCode());
        assertEquals("name is empty", ex.getMessage());
    }

    @Test
    public void test_paramsInvalid_with_resultCode() {
        ParamsInvalidException ex = new ParamsInvalidException(CommonResultCodeEnum.PARAMETER_ILLEGAL);
        assertEquals(10001, ex.getCode());
    }

    // --- DownstreamException ---

    @Test
    public void test_downstream_with_string_message() {
        DownstreamException ex = new DownstreamException("db timeout");
        assertEquals(CommonResultCodeEnum.INTEGRATION_ERROR.getCode(), ex.getCode());
        assertEquals("db timeout", ex.getMessage());
        assertTrue(ex.isRetryable());
    }

    // --- SystemLogicException ---

    @Test
    public void test_systemLogic_with_string_message() {
        SystemLogicException ex = new SystemLogicException("unreachable branch");
        assertEquals(CommonResultCodeEnum.SYSTEM_ERROR.getCode(), ex.getCode());
        assertEquals("unreachable branch", ex.getMessage());
    }

    // --- Type distinction ---

    @Test
    public void test_can_distinguish_sub_exception_types() {
        CommonException ex = new ParamsInvalidException("bad param");
        try {
            throw ex;
        } catch (ParamsInvalidException e) {
            assertEquals("bad param", e.getMessage());
        } catch (DownstreamException e) {
            fail("should not reach here");
        }

        ex = new DownstreamException("downstream fail");
        try {
            throw ex;
        } catch (ParamsInvalidException e) {
            fail("should not reach here");
        } catch (DownstreamException e) {
            assertEquals("downstream fail", e.getMessage());
        }

        ex = new SystemLogicException("logic error");
        try {
            throw ex;
        } catch (SystemLogicException e) {
            assertEquals("logic error", e.getMessage());
        }
    }
}
