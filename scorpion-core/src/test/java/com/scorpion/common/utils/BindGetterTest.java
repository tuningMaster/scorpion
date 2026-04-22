package com.scorpion.common.utils;

import com.scorpion.common.utils.invoke.BindGetter;
import org.junit.Test;

import static org.junit.Assert.*;

public class BindGetterTest {

    public static class Person {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

    public static class WithBoolField {
        private boolean active;

        public WithBoolField(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }
    }

    // --- field() with getter ---

    @Test
    public void test_field_via_getter() {
        BindGetter<Person, String> getter = BindGetter.field(Person.class, "name", String.class);
        Person p = new Person("Alice", 30);
        assertEquals("Alice", getter.get(p));
    }

    @Test
    public void test_field_int_getter() {
        BindGetter<Person, Integer> getter = BindGetter.field(Person.class, "age", Integer.class);
        Person p = new Person("Bob", 25);
        assertEquals(Integer.valueOf(25), getter.get(p));
    }

    // --- field() via boolean isXxx ---

    @Test
    public void test_field_boolean_isXxx() {
        BindGetter<WithBoolField, Boolean> getter = BindGetter.field(WithBoolField.class, "active", Boolean.class);
        WithBoolField obj = new WithBoolField(true);
        assertTrue(getter.get(obj));
    }

    // --- constant() ---

    @Test
    public void test_constant() {
        BindGetter<Person, String> getter = BindGetter.constant("fixed");
        Person p = new Person("Alice", 30);
        assertEquals("fixed", getter.get(p));
    }

    // --- field() direct field access ---

    @Test
    public void test_field_direct_field_access() {
        BindGetter<Person, String> getter = BindGetter.field(Person.class, "name", String.class);
        Person p = new Person("Charlie", 40);
        assertEquals("Charlie", getter.get(p));
    }
}
