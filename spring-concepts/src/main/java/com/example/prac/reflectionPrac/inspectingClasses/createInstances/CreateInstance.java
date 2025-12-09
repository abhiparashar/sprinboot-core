package com.example.prac.reflectionPrac.inspectingClasses.createInstances;

import com.example.prac.reflectionPrac.inspectingClasses.BankAccount;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class CreateInstance {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        // Using No-Arg Constructor
        Class<?>clazz = BankAccount.class;

        // Modern way (Java 9+)
        Constructor<?>constructor = clazz.getDeclaredConstructor();
        Object account = constructor.newInstance();

        // Using Parameterized Constructor
        Constructor<?>constructor1 = clazz.getDeclaredConstructor(String.class, double.class);
        Object obj = constructor1.newInstance("ACC-001",1000.00);

        // When Spring sees @Component on a class, internally it does something like:
        Class<?>clazz1 = Class.forName("com.example.prac.reflectionPrac.inspectingClasses.BankAccount");
        Constructor<?> constructor2 = clazz1.getDeclaredConstructor();
        Object bean = constructor2.newInstance();
        // Then it registers this as a bean
    }
}
