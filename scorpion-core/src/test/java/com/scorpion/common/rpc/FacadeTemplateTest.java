package com.scorpion.common.rpc;

import com.scorpion.common.exception.CommonException;
import com.scorpion.common.exception.CommonResultCodeEnum;
import lombok.Data;
import org.junit.Test;

import static org.junit.Assert.*;

public class FacadeTemplateTest {

    @Data
    public static class TestResponse {
        private String data;
        private Result result;
    }

    @Data
    public static class ErrorResponse {
        private Result result;
    }

    // --- Normal path ---

    @Test
    public void test_execute_normal_path() {
        FacadeCallback<String, TestResponse> callback = new FacadeCallback<String, TestResponse>() {
            @Override
            public String identifier() {
                return "test-normal";
            }

            @Override
            public TestResponse execute(String request) {
                TestResponse response = new TestResponse();
                response.setData("hello:" + request);
                return response;
            }
        };

        TestResponse result = FacadeTemplate.execute("world", callback);
        assertEquals("hello:world", result.getData());
        assertNotNull(result.getResult());
        assertTrue(result.getResult().isSuccess());
    }

    // --- CommonException path ---

    @Test
    public void test_execute_commonException_path() {
        FacadeCallback<String, TestResponse> callback = new FacadeCallback<String, TestResponse>() {
            @Override
            public String identifier() {
                return "test-commonEx";
            }

            @Override
            public TestResponse execute(String request) {
                throw new CommonException(CommonResultCodeEnum.PARAMETER_ILLEGAL, "bad param");
            }
        };

        TestResponse result = FacadeTemplate.execute("world", callback);
        assertNotNull(result.getResult());
        assertFalse(result.getResult().isSuccess());
        assertEquals(CommonResultCodeEnum.PARAMETER_ILLEGAL.getCode(), result.getResult().getCode());
        assertEquals("bad param", result.getResult().getMessage());
    }

    // --- Unknown exception path (execute) ---

    @Test
    public void test_execute_unknownException_shows_stack() {
        FacadeCallback<String, TestResponse> callback = new FacadeCallback<String, TestResponse>() {
            @Override
            public String identifier() {
                return "test-unknownEx";
            }

            @Override
            public TestResponse execute(String request) {
                throw new RuntimeException("NPE happened");
            }
        };

        TestResponse result = FacadeTemplate.execute("world", callback);
        assertNotNull(result.getResult());
        assertFalse(result.getResult().isSuccess());
        // When returnExStack=true and exception is not CommonException, code is not set
        assertEquals(0, result.getResult().getCode());
    }

    // --- executeForFE hides stack ---

    @Test
    public void test_executeForFE_hides_stack() {
        FacadeCallback<String, TestResponse> callback = new FacadeCallback<String, TestResponse>() {
            @Override
            public String identifier() {
                return "test-forFE";
            }

            @Override
            public TestResponse execute(String request) {
                throw new RuntimeException("secret details");
            }
        };

        TestResponse result = FacadeTemplate.executeForFE("world", callback);
        assertNotNull(result.getResult());
        assertFalse(result.getResult().isSuccess());
        assertEquals(CommonResultCodeEnum.SYSTEM_ERROR.getCode(), result.getResult().getCode());
        assertEquals("系统异常", result.getResult().getMessage());
    }

    // --- checkParameters is called ---

    @Test
    public void test_execute_checkParameters_fails() {
        FacadeCallback<String, TestResponse> callback = new FacadeCallback<String, TestResponse>() {
            @Override
            public String identifier() {
                return "test-check";
            }

            @Override
            public void checkParameters(String request) {
                if (request == null) {
                    throw new CommonException(CommonResultCodeEnum.PARAMETER_ILLEGAL, "request is null");
                }
            }

            @Override
            public TestResponse execute(String request) {
                return new TestResponse();
            }
        };

        TestResponse result = FacadeTemplate.execute(null, callback);
        assertNotNull(result.getResult());
        assertFalse(result.getResult().isSuccess());
        assertEquals(CommonResultCodeEnum.PARAMETER_ILLEGAL.getCode(), result.getResult().getCode());
    }
}
