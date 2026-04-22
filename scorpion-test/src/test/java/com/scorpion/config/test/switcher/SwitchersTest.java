package com.scorpion.config.test.switcher;

import com.scorpion.config.api.switcher.SwitcherConfig;
import com.scorpion.config.api.switcher.SwitcherModeEnum;
import com.scorpion.config.api.switcher.policy.DefaultCheckPolicy;
import org.junit.Test;

import static org.junit.Assert.*;

public class SwitchersTest {

    @Test
    public void testSwitcherModeEnum_values() {
        assertEquals(3, SwitcherModeEnum.values().length);
        assertNotNull(SwitcherModeEnum.valueOf("OLD"));
        assertNotNull(SwitcherModeEnum.valueOf("NEW"));
        assertNotNull(SwitcherModeEnum.valueOf("BOTH"));
    }

    @Test
    public void testDefaultCheckPolicy_bothSameResult() {
        DefaultCheckPolicy policy = DefaultCheckPolicy.INSTANCE;
        SwitcherConfig config = new SwitcherConfig();
        config.setMonitorEnable(false);

        String result = policy.doPolicy(config, "result", "result", null, null);
        assertEquals("result", result);
    }

    @Test
    public void testDefaultCheckPolicy_bothDifferentResult() {
        DefaultCheckPolicy policy = DefaultCheckPolicy.INSTANCE;
        SwitcherConfig config = new SwitcherConfig();
        config.setMonitorEnable(false);

        String result = policy.doPolicy(config, "newResult", "oldResult", null, null);
        // In BOTH mode, default policy returns oldResult
        assertEquals("oldResult", result);
    }

    @Test(expected = RuntimeException.class)
    public void testDefaultCheckPolicy_bothThrowException() {
        DefaultCheckPolicy policy = DefaultCheckPolicy.INSTANCE;
        SwitcherConfig config = new SwitcherConfig();
        config.setMonitorEnable(false);

        RuntimeException newEx = new RuntimeException("new error");
        RuntimeException oldEx = new RuntimeException("old error");
        policy.doPolicy(config, null, null, newEx, oldEx);
    }

    @Test(expected = RuntimeException.class)
    public void testDefaultCheckPolicy_oldThrowsException() {
        DefaultCheckPolicy policy = DefaultCheckPolicy.INSTANCE;
        SwitcherConfig config = new SwitcherConfig();
        config.setMonitorEnable(false);

        RuntimeException oldEx = new RuntimeException("old error");
        policy.doPolicy(config, null, null, null, oldEx);
    }

    @Test(expected = RuntimeException.class)
    public void testDefaultCheckPolicy_newThrowsException() {
        DefaultCheckPolicy policy = DefaultCheckPolicy.INSTANCE;
        SwitcherConfig config = new SwitcherConfig();
        config.setMonitorEnable(false);

        RuntimeException newEx = new RuntimeException("new error");
        policy.doPolicy(config, null, null, newEx, null);
    }
}
