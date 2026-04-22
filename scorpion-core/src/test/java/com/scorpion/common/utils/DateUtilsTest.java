package com.scorpion.common.utils;

import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.*;

public class DateUtilsTest {

    @Test
    public void test_formatDate_default() {
        Date date = new Date(0); // 1970-01-01 08:00:00 CST (UTC+8)
        String result = DateUtils.formatDate(date);
        assertEquals("1970-01-01 08:00:00", result);
    }

    @Test
    public void test_formatDate_null_returns_null() {
        assertNull(DateUtils.formatDate(null));
    }

    @Test
    public void test_formatDate_custom_pattern() {
        Date date = new Date(0);
        assertEquals("19700101", DateUtils.formatDate(date, DateUtils.DATE_PATTERN_SHORT_DATE1));
    }

    @Test
    public void test_parseDate_default() {
        Date date = DateUtils.parseDate("2026-04-20 21:00:00");
        assertNotNull(date);
        assertEquals("2026-04-20 21:00:00", DateUtils.formatDate(date));
    }

    @Test
    public void test_parseDate_null_returns_null() {
        assertNull(DateUtils.parseDate(null));
    }

    @Test
    public void test_parseDate_invalid_returns_null() {
        assertNull(DateUtils.parseDate("not-a-date"));
    }

    @Test
    public void test_formatDate_null_pattern() {
        assertNull(DateUtils.formatDate(null, null));
    }

    @Test
    public void test_parseDate_null_pattern() {
        assertNull(DateUtils.parseDate("2026-04-20", null));
    }
}
