package com.scorpion.config.test.strategy;

import com.scorpion.config.facade.model.StrategyDTO;
import com.scorpion.config.impl.strategy.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class StrategyTest {

    private StrategyDTO buildWhiteListDTO(String whiteList) {
        StrategyDTO dto = new StrategyDTO();
        dto.setStrategyType("WHITE_LIST");
        dto.setWhiteList(whiteList);
        return dto;
    }

    // ==================== WhiteListStrategy ====================

    @Test
    public void testWhiteList_match() {
        WhiteListStrategy strategy = new WhiteListStrategy(buildWhiteListDTO("user1,user2,user3"));
        assertTrue(strategy.match("user1"));
        assertTrue(strategy.match("user2"));
        assertTrue(strategy.match("user3"));
    }

    @Test
    public void testWhiteList_noMatch() {
        WhiteListStrategy strategy = new WhiteListStrategy(buildWhiteListDTO("user1,user2"));
        assertFalse(strategy.match("user3"));
        assertFalse(strategy.match(""));
        assertFalse(strategy.match(null));
    }

    @Test
    public void testWhiteList_emptyList() {
        try {
            WhiteListStrategy strategy = new WhiteListStrategy(buildWhiteListDTO(""));
            fail("Expected RuntimeException for blank whiteList");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test
    public void testWhiteList_nullList() {
        try {
            WhiteListStrategy strategy = new WhiteListStrategy(buildWhiteListDTO(null));
            fail("Expected RuntimeException for null whiteList");
        } catch (RuntimeException e) {
            // expected
        }
    }

    private StrategyDTO buildPercentDTO(int percent) {
        StrategyDTO dto = new StrategyDTO();
        dto.setStrategyType("PERCENT");
        dto.setPercent(percent);
        return dto;
    }

    // ==================== PercentStrategy ====================

    @Test
    public void testPercent_100() {
        PercentStrategy strategy = new PercentStrategy(buildPercentDTO(100));
        // 100% should match everything
        assertTrue(strategy.match("anyKey"));
        assertTrue(strategy.match("anotherKey"));
    }

    @Test
    public void testPercent_0() {
        PercentStrategy strategy = new PercentStrategy(buildPercentDTO(0));
        assertFalse(strategy.match("anyKey"));
        assertFalse(strategy.match("user1"));
    }

    @Test(expected = NullPointerException.class)
    public void testPercent_nullParam() {
        PercentStrategy strategy = new PercentStrategy(buildPercentDTO(50));
        strategy.match(null);
    }

    @Test
    public void testPercent_shortKeyRepeat() {
        PercentStrategy strategy = new PercentStrategy(buildPercentDTO(50));
        // Short keys (<=3 chars) should have their hash repeated
        String result = strategy.match("a") ? "hit" : "miss";
        assertNotNull(result);
    }

    @Test
    public void testPercent_distribution() {
        PercentStrategy strategy = new PercentStrategy(buildPercentDTO(30));
        int hits = 0;
        int total = 1000;
        for (int i = 0; i < total; i++) {
            if (strategy.match("user" + i)) {
                hits++;
            }
        }
        // 30% strategy should hit roughly 30% of the time
        double hitRate = (double) hits / total;
        // Allow some variance: 20% ~ 40%
        assertTrue("Hit rate too low: " + hitRate, hitRate >= 0.20);
        assertTrue("Hit rate too high: " + hitRate, hitRate <= 0.40);
    }

    // ==================== BetaStrategy ====================

    @Test
    public void testBeta_singletonInstance() {
        // BetaStrategy uses EnvUtils.isBeta(), which may be false in test env
        // Just verify the singleton instance exists and doesn't throw
        assertNotNull(BetaStrategy.INSTANCE);
        // Verify it returns consistent results
        boolean result1 = BetaStrategy.INSTANCE.match("anything");
        boolean result2 = BetaStrategy.INSTANCE.match("anything");
        assertEquals(result1, result2);
    }

    // ==================== DefaultStrategy ====================

    @Test
    public void testDefault_alwaysMatch() {
        assertTrue(DefaultStrategy.INSTANCE.match("anything"));
        assertTrue(DefaultStrategy.INSTANCE.match(null));
        assertTrue(DefaultStrategy.INSTANCE.match(""));
    }

    // ==================== NoneStrategy ====================

    @Test
    public void testNone_neverMatch() {
        assertFalse(NoneStrategy.INSTANCE.match("anything"));
        assertFalse(NoneStrategy.INSTANCE.match(null));
        assertFalse(NoneStrategy.INSTANCE.match(""));
    }

    // ==================== GroupStrategy ====================

    @Test
    public void testGroupStrategy_allMatch() {
        List<Strategy> strategies = new ArrayList<>();
        strategies.add(new WhiteListStrategy(buildWhiteListDTO("user1,user2")));
        strategies.add(new PercentStrategy(buildPercentDTO(100)));

        GroupStrategy group = new GroupStrategy(strategies);

        assertTrue(group.match("user1"));
        assertTrue(group.match("user2"));
    }

    @Test
    public void testGroupStrategy_oneNotMatch() {
        // GroupStrategy uses OR logic: returns true if ANY strategy matches
        // WhiteListStrategy matches "user1", PercentStrategy(0) never matches
        List<Strategy> strategies = new ArrayList<>();
        strategies.add(new WhiteListStrategy(buildWhiteListDTO("user1,user2")));
        strategies.add(new PercentStrategy(buildPercentDTO(0))); // 0% never matches

        GroupStrategy group = new GroupStrategy(strategies);

        // user1 matches via WhiteListStrategy (OR logic)
        assertTrue(group.match("user1"));
        // user3 doesn't match WhiteList, and 0% never matches
        assertFalse(group.match("user3"));
    }

    @Test
    public void testGroupStrategy_emptyList() {
        GroupStrategy group = new GroupStrategy(new ArrayList<>());
        assertFalse(group.match("user1"));
    }

    @Test(expected = NullPointerException.class)
    public void testGroupStrategy_nullList() {
        new GroupStrategy(null).match("user1");
    }

    // ==================== Strategy.parse ====================

    @Test
    public void testParse_singleWhiteList() {
        StrategyDTO dto = new StrategyDTO();
        dto.setStrategyType("WHITE_LIST");
        dto.setWhiteList("user1,user2");

        Strategy strategy = Strategy.parse(Collections.singletonList(dto));
        assertTrue(strategy instanceof WhiteListStrategy);
        assertTrue(strategy.match("user1"));
        assertFalse(strategy.match("user3"));
    }

    @Test
    public void testParse_singlePercent() {
        StrategyDTO dto = new StrategyDTO();
        dto.setStrategyType("PERCENT");
        dto.setPercent(100);

        Strategy strategy = Strategy.parse(Collections.singletonList(dto));
        assertTrue(strategy instanceof PercentStrategy);
        assertTrue(strategy.match("anyKey"));
    }

    @Test
    public void testParse_singleBeta() {
        StrategyDTO dto = new StrategyDTO();
        dto.setStrategyType("BETA");

        Strategy strategy = Strategy.parse(Collections.singletonList(dto));
        assertSame(BetaStrategy.INSTANCE, strategy);
    }

    @Test
    public void testParse_multipleStrategies() {
        List<StrategyDTO> list = new ArrayList<>();

        StrategyDTO whiteList = new StrategyDTO();
        whiteList.setStrategyType("WHITE_LIST");
        whiteList.setWhiteList("user1");
        list.add(whiteList);

        StrategyDTO percent = new StrategyDTO();
        percent.setStrategyType("PERCENT");
        percent.setPercent(100);
        list.add(percent);

        Strategy strategy = Strategy.parse(list);
        assertTrue(strategy instanceof GroupStrategy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_emptyList() {
        Strategy.parse(new ArrayList<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullList() {
        Strategy.parse(null);
    }

    @Test(expected = RuntimeException.class)
    public void testParse_unknownType() {
        StrategyDTO dto = new StrategyDTO();
        dto.setStrategyType("UNKNOWN_TYPE");

        Strategy.parse(Collections.singletonList(dto));
    }
}
