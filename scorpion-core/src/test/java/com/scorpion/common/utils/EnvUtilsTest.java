package com.scorpion.common.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class EnvUtilsTest {

    @Test
    public void test_getEnv_not_null() {
        assertNotNull(EnvUtils.getEnv());
    }

    @Test
    public void test_env_methods_return_boolean() {
        // 环境取决于系统配置，只需验证方法不抛异常且返回布尔值
        boolean dev = EnvUtils.isDev();
        boolean sit = EnvUtils.isSit();
        boolean beta = EnvUtils.isBeta();
        boolean prod = EnvUtils.isProd();
        boolean unknown = EnvUtils.isUnknown();

        // 至少有一个为 true 或 unknown
        assertTrue(dev || sit || beta || prod || unknown);
    }
}
