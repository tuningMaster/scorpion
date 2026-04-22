package com.scorpion.common.exception;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 断言工具类，统一参数校验风格
 */
public abstract class AssertUtils {

    // ========== 核心方法 ==========

    public static void isTrue(boolean expression, ResultCode resultCode) {
        Objects.requireNonNull(resultCode, "resultCode must not be null");
        if (!expression) {
            throw new CommonException(resultCode);
        }
    }

    public static void isTrue(boolean expression, ResultCode resultCode, String message) {
        Objects.requireNonNull(resultCode, "resultCode must not be null");
        if (!expression) {
            throw new CommonException(resultCode, message);
        }
    }

    // ========== isTrue / isFalse ==========

    public static void isFalse(boolean expression, ResultCode resultCode) {
        isTrue(!expression, resultCode);
    }

    public static void isFalse(boolean expression, ResultCode resultCode, String message) {
        isTrue(!expression, resultCode, message);
    }

    // ========== isNull / isNotNull ==========

    public static void isNull(Object object, ResultCode resultCode) {
        isTrue(object == null, resultCode);
    }

    public static void isNull(Object object, ResultCode resultCode, String message) {
        isTrue(object == null, resultCode, message);
    }

    public static void isNotNull(Object object, ResultCode resultCode) {
        isTrue(object != null, resultCode);
    }

    public static void isNotNull(Object object, ResultCode resultCode, String message) {
        isTrue(object != null, resultCode, message);
    }

    // ========== isBlank / isNotBlank ==========

    public static void isBlank(CharSequence text, ResultCode resultCode) {
        isTrue(StringUtils.isBlank(text), resultCode);
    }

    public static void isBlank(CharSequence text, ResultCode resultCode, String message) {
        isTrue(StringUtils.isBlank(text), resultCode, message);
    }

    public static void isNotBlank(CharSequence text, ResultCode resultCode) {
        isTrue(StringUtils.isNotBlank(text), resultCode);
    }

    public static void isNotBlank(CharSequence text, ResultCode resultCode, String message) {
        isTrue(StringUtils.isNotBlank(text), resultCode, message);
    }

    // ========== isNotEmpty ==========

    public static void isNotEmpty(Object[] array, ResultCode resultCode) {
        isTrue(array != null && array.length > 0, resultCode);
    }

    public static void isNotEmpty(Object[] array, ResultCode resultCode, String message) {
        isTrue(array != null && array.length > 0, resultCode, message);
    }

    public static void isNotEmpty(Collection<?> collection, ResultCode resultCode) {
        isTrue(collection != null && !collection.isEmpty(), resultCode);
    }

    public static void isNotEmpty(Collection<?> collection, ResultCode resultCode, String message) {
        isTrue(collection != null && !collection.isEmpty(), resultCode, message);
    }

    public static void isNotEmpty(Map<?, ?> map, ResultCode resultCode) {
        isTrue(map != null && !map.isEmpty(), resultCode);
    }

    public static void isNotEmpty(Map<?, ?> map, ResultCode resultCode, String message) {
        isTrue(map != null && !map.isEmpty(), resultCode, message);
    }

    // ========== isEquals / isNotEquals ==========

    public static void isEquals(Object expected, Object actual, ResultCode resultCode) {
        isTrue(Objects.equals(expected, actual), resultCode);
    }

    public static void isEquals(Object expected, Object actual, ResultCode resultCode, String message) {
        isTrue(Objects.equals(expected, actual), resultCode, message);
    }

    public static void isNotEquals(Object expected, Object actual, ResultCode resultCode) {
        isTrue(ObjectUtils.notEqual(expected, actual), resultCode);
    }

    public static void isNotEquals(Object expected, Object actual, ResultCode resultCode, String message) {
        isTrue(ObjectUtils.notEqual(expected, actual), resultCode, message);
    }
}
