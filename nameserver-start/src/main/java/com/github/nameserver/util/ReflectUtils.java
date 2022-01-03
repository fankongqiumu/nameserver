package com.github.nameserver.util;

import com.github.nameserver.exceptioin.ParamterParseExeception;

import java.lang.reflect.Field;

public class ReflectUtils {

    public static boolean isPrimitiveOrWrapper(Class<?> clazz){
        boolean isPrimitive = clazz.isPrimitive();
        if (isPrimitive){
            return true;
        }
        try {
            Field typeField = clazz.getField("TYPE");
            typeField.setAccessible(true);
            Class<?> typeFieldClass = (Class<?>) typeField.get("null");
            return typeFieldClass.isPrimitive();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ParamterParseExeception(e.getMessage(), e);
        }
    }
}
