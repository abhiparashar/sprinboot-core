package com.example.prac.reflectionPrac.inspectingClasses;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class Calculator{
    public int num1;
    public int num2;

    public int add(int a, int b){
        return a+b;
    }

    public int multiply(int a, int b){
        return a*b;
    }

    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}

public class MethodCaller {
    private static Class<?> toPrimitiveType(Class<?> clazz) {
        if (clazz == Integer.class) return int.class;
        if (clazz == Double.class) return double.class;
        if (clazz == Boolean.class) return boolean.class;
        if (clazz == Long.class) return long.class;
        if (clazz == Float.class) return float.class;
        if (clazz == Short.class) return short.class;
        if (clazz == Byte.class) return byte.class;
        if (clazz == Character.class) return char.class;
        return clazz;
    }

    public static Object callMethod(Object object, String method, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            Class<?> clazz = object.getClass();

            // Build array of parameter types from the arguments
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = toPrimitiveType(args[i].getClass());
            }

            // Get the method by name and parameter types
            Method method1 = clazz.getDeclaredMethod(method, paramTypes);
            method1.setAccessible(true);

            // Invoke and return result
            return method1.invoke(object, args);

        } catch (NoSuchMethodException e) {
            System.out.println("Method '" + method + "' not found");
        } catch (IllegalAccessException e) {
            System.out.println("Cannot access method '" + method + "'");
        } catch (InvocationTargetException e) {
            System.out.println("Method threw exception: " + e.getCause());
        }
        return null;
    }

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Calculator calc = new Calculator();

        Object result1 = callMethod(calc, "add", 5, 3);
        System.out.println("add(5, 3) = " + result1);

        Object result2 = callMethod(calc, "multiply", 4, 7);
        System.out.println("multiply(4, 7) = " + result2);

        Object result3 = callMethod(calc, "greet", "World");
        System.out.println("greet(\"World\") = " + result3);
    }
}
