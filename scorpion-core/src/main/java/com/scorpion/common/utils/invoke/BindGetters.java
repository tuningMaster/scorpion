package com.scorpion.common.utils.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * 高性能 Getter 绑定实现（MethodHandle）
 */
class BindGetters {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    static <T, V> BindGetter<T, V> field(Class<T> targetClass, String fieldName, Class<V> valueType) {
        String getterName = "get" + capitalize(fieldName);
        String boolGetterName = "is" + capitalize(fieldName);

        try {
            MethodHandle mh = findMethod(targetClass, getterName);
            if (mh != null && valueType.isAssignableFrom(mh.type().returnType())) {
                return createFieldGetter(targetClass, mh);
            }
            if (boolean.class == valueType || Boolean.class == valueType) {
                mh = findMethod(targetClass, boolGetterName);
                if (mh != null) {
                    return createFieldGetter(targetClass, mh);
                }
            }
            mh = findField(targetClass, fieldName);
            if (mh != null) {
                return createFieldGetter(targetClass, mh);
            }
            throw new IllegalArgumentException("Cannot find getter or field: " + fieldName + " in " + targetClass.getName());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static <T, V> BindGetter<T, V> fieldOrMethod(Class<T> targetClass, String name, Class<V> valueType) {
        try {
            MethodHandle mh = findMethod(targetClass, name);
            if (mh != null) {
                return createFieldGetter(targetClass, mh);
            }
        } catch (Throwable ignored) {
        }
        return field(targetClass, name, valueType);
    }

    @SuppressWarnings("unchecked")
    private static <T, V> BindGetter<T, V> createFieldGetter(Class<T> targetClass, MethodHandle mh) {
        MethodHandle typed = mh.asType(MethodType.methodType(Object.class, Object.class));
        return target -> {
            try {
                return (V) typed.invoke(target);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static MethodHandle findMethod(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getMethod(methodName);
            return LOOKUP.unreflect(method);
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static MethodHandle findField(Class<?> clazz, String fieldName) {
        try {
            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return LOOKUP.unreflectGetter(field);
        } catch (Throwable ignored) {
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            return findField(superclass, fieldName);
        }
        return null;
    }

    private static String capitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
