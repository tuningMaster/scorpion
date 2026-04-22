package com.scorpion.config.api.switcher;

import com.scorpion.config.api.holder.ConfigHolder;
import com.scorpion.config.api.switcher.policy.CheckPolicy;
import com.scorpion.config.api.switcher.policy.DefaultCheckPolicy;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * 切流工具类
 * 基于配置的新老逻辑切流能力，支持 OLD/NEW/BOTH 三种模式
 */
@Slf4j
public class Switchers {
    /**
     * 切流调用
     *
     * @param params  灰度参数
     * @param id      切流key， 对应配置平台的配置id
     * @param callNew 新逻辑
     * @param callOld 旧逻辑
     * @return 决策后的结果
     */
    public static <T> T call(String params, String id, Supplier<T> callNew, Supplier<T> callOld, CheckPolicy policy) {
        SwitcherConfig config = ConfigHolder.findById(SwitcherConfig.class, params, id);

        // config == null → 执行 old（安全兜底）
        if (config != null && config.isMonitorEnable()) {
            // 监控上报
        }

        if (config == null) {
            return callOld.get();
        }

        if (config.getMode() == null) {
            log.warn("Switchers.call config is illegal, mode is null: {}, execute old...", config);
            return callOld.get();
        }

        if (config.getMode() == SwitcherModeEnum.OLD) {
            return callOld.get();
        } else if (config.getMode() == SwitcherModeEnum.NEW) {
            return callNew.get();
        } else if (config.getMode() == SwitcherModeEnum.BOTH) {
            Throwable newThrowable = null;
            Throwable oldThrowable = null;
            T oldRes = null;
            T newRes = null;

            try {
                oldRes = callOld.get();
            } catch (Throwable t) {
                oldThrowable = t;
            }

            try {
                newRes = callNew.get();
            } catch (Throwable t) {
                newThrowable = t;
            }

            return policy.doPolicy(config, newRes, oldRes, oldThrowable, newThrowable);
        } else {
            log.warn("Swithcers.call config is illegal, unknown mode: {}. execute old...", config.getMode());
            return callOld.get();
        }
    }


    /**
     * 调用切流，使用默认决策
     *
     * @param params  灰度参数
     * @param id      切流key，对应配置平台的配置id
     * @param callNew 新方法调用 如果要返回null 传入 () -> null, 而不是null
     * @param callOld 旧方法调用 如果要返回null 传入 () -> null, 而不是null
     * @param <T>     返回值类型
     * @return 最终决策执行的方法
     */
    public static <T> T call(String params, String id, Supplier<T> callNew, Supplier<T> callOld) {
        return call(params, id, callNew, callOld, DefaultCheckPolicy.INSTANCE);
    }

    /**
     * 命中切流返回 true（即 mode 不是 OLD）
     */
    public static boolean hit(String params, String id) {
        return call(params, id, () -> true, () -> false);
    }

    /**
     * 未命中返回 true
     */
    public static boolean miss(String params, String id) {
        return call(params, id, () -> false, () -> true);
    }

    /**
     * 无返回值版本
     */
    public static void callRun(String params, String id, Runnable runNew, Runnable runOld) {
        call(params, id,
                () -> {
                    runNew.run();
                    return null;
                },
                () -> {
                    runOld.run();
                    return null;
                });
    }
}
