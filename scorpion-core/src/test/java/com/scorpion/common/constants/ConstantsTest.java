package com.scorpion.common.constants;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConstantsTest {

    @Test
    public void test_all_constants_have_expected_values() {
        assertEquals("", Constants.EMPTY);
        assertEquals(",", Constants.COMMA);
        assertEquals(".", Constants.POINT);
        assertEquals("*", Constants.STAR);
        assertEquals("/", Constants.SLASH);
        assertEquals("\\", Constants.BACK_SLASH);
        assertEquals("_", Constants.UNDERLINE);
        assertEquals(";", Constants.SEMICOLON);
        assertEquals(":", Constants.COLON);
        assertEquals("+", Constants.PLUS);
        assertEquals("-", Constants.MINUS);
    }
}
