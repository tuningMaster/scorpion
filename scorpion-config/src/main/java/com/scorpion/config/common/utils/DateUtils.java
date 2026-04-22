package com.scorpion.config.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 */
public final class DateUtils {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private DateUtils() {
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(DATE_PATTERN).format(date);
    }

    public static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat(DATE_PATTERN).parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse date: " + dateStr, e);
        }
    }
}
