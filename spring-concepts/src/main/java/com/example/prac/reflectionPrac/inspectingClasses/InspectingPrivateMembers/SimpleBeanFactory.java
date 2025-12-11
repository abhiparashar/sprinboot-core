package com.example.prac.reflectionPrac.inspectingClasses.InspectingPrivateMembers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class SimpleBeanFactory {
    public static Object createBean(String className) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?>clazz = Class.forName(className);
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        return constructor.newInstance();
    }
    public static void main(String[] args) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Test with ArrayList
        Object list = SimpleBeanFactory.createBean("java.util.ArrayList");
        System.out.println("Created: " + list.getClass().getSimpleName());

        // Test with HashMap
        Object map = SimpleBeanFactory.createBean("java.util.HashMap");
        System.out.println("Created: " + map.getClass().getSimpleName());

        // Test with StringBuilder
        Object sb = SimpleBeanFactory.createBean("java.lang.StringBuilder");
        System.out.println("Created: " + sb.getClass().getSimpleName());

        // Verify it works
        if( list instanceof List){
            ((List<String>) list).add("Reflection is powerful!");
            System.out.println("List contents: " + list);
        }
    }
}
