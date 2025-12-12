package com.example.prac.reflectionPrac.basic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class ObjectCloner {
    public static <T> T clone(T object) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?>clazz = object.getClass();
        Constructor<?>constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object copy = constructor.newInstance();
        Field[]fields = clazz.getDeclaredFields();
        for (Field field:fields){
            field.setAccessible(true);
            Object value = field.get(object);
            field.set(copy, value);
        }
        return (T)copy;
    }
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Person p1 = new Person("John", 25);
        Person p2 = clone(p1);

        System.out.println("Original: " + p1.getName() + ", " + p1.getAge());
        System.out.println("Clone: " + p2.getName() + ", " + p2.getAge());
        System.out.println("Same object? " + (p1 == p2));
    }
}
