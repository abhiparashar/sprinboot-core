package com.example.prac.reflectionPrac.inspectingClasses;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class InspectingClass {
    public static void main(String[] args) throws NoSuchMethodException {
        Class<?>clazz = BankAccount.class;

        // Get ALL declared fields (including private)
        Field[]fields = clazz.getDeclaredFields();
        for(Field field : fields){
            System.out.println("Field:"+field.getName()+", Type: "+field.getType().getSimpleName());
        }

        // All declared methods (including private, excluding inherited)
        Method[] allMethods = clazz.getDeclaredMethods();
        for (Method method:allMethods){
            System.out.println("Method:" + method.getName() + ", returns " + method.getReturnType().getSimpleName());
        }

        // Get specific method by name and parameter types
        Method depositMethod = clazz.getMethod("deposit", double.class);

        //Inspecting Constructors
        Constructor<?>[]constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors){
            System.out.println("Constructor params: " + constructor.getParameterCount());
        }
    }
}
