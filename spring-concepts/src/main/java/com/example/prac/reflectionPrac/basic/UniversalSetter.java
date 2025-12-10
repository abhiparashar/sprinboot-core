package com.example.prac.reflectionPrac.basic;

import java.lang.reflect.Field;

public class UniversalSetter {
    public static void setField(Object object, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Class<?>clazz = object.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
    public static void main(String[] args) throws IllegalAccessException, NoSuchFieldException {
        Person p = new Person();

        setField(p, "name", "Alice");
        setField(p, "age", 30);

        // Verify it worked
        System.out.println("Name: " + p.getName());
        System.out.println("Age: " + p.getAge());
    }
}
