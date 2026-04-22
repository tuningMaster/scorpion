package com.scorpion.common.utils;

/**
 * 运行环境判断工具
 */
public abstract class EnvUtils {

    private static String env;

    static {
        env = System.getProperty("env");
        if (env == null) {
            env = System.getenv("ENV");
        }
        if (env == null) {
            env = System.getenv("XHS_ENV");
        }
        if (env == null) {
            env = "unknown";
        }
    }

    public static String getEnv() {
        return env;
    }

    public static boolean isDev() {
        return "dev".equalsIgnoreCase(env);
    }

    public static boolean isSit() {
        return "sit".equalsIgnoreCase(env);
    }

    public static boolean isBeta() {
        String lower = env.toLowerCase();
        return "beta".equals(lower) || "staging".equals(lower);
    }

    public static boolean isProd() {
        String lower = env.toLowerCase();
        return "prod".equals(lower) || "production".equals(lower);
    }

    public static boolean isUnknown() {
        return !isDev() && !isSit() && !isBeta() && !isProd();
    }
}
