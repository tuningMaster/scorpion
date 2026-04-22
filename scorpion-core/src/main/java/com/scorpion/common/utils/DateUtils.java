package com.scorpion.common.utils;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;

/**
 * 日期格式化/解析工具，使用 FastDateFormat（线程安全）
 */
public abstract class DateUtils {

    public static final String DATE_PATTERN_COMPLETE = "yyyy-MM-dd HH:mm:ss SSS";
    public static final String DATE_PATTERN_COMMON = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN_SHORT_DATE1 = "yyyyMMdd";
    public static final String DATE_PATTERN_SHORT_DATE2 = "yyyy-MM-dd";

    private static final FastDateFormat DEFAULT_FORMAT = FastDateFormat.getInstance(DATE_PATTERN_COMMON);

    /**
     * 使用默认格式格式化日期
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return DEFAULT_FORMAT.format(date);
    }

    /**
     * 使用指定格式格式化日期
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return FastDateFormat.getInstance(pattern).format(date);
    }

    /**
     * 使用默认格式解析日期字符串
     */
    public static Date parseDate(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        try {
            return DEFAULT_FORMAT.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 使用指定格式解析日期字符串
     */
    public static Date parseDate(String dateStr, String pattern) {
        if (dateStr == null || pattern == null) {
            return null;
        }
        try {
            return FastDateFormat.getInstance(pattern).parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
}
