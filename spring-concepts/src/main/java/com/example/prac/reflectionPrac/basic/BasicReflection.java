package com.example.prac.reflectionPrac.basic;

import java.lang.reflect.Method;

public class BasicReflection {
    public static void main(String[] args) throws Exception {
        // Without reflection - you must know the class at compile time
//        User user = new User();
//        user.setName("John");

        // With reflection - you can work with ANY class
        Object obj = Class.forName("com.example.prac.reflectionPrac.basic.User")
                .getDeclaredConstructor()
                .newInstance();

        Method setter = obj.getClass().getMethod("setName", String.class);
        setter.invoke(obj, "John");
        Method getter = obj.getClass().getDeclaredMethod("getName");
        System.out.println(getter.invoke(obj));
    }
}

