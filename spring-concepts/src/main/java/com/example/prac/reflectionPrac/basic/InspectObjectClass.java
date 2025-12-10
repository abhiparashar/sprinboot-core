package com.example.prac.reflectionPrac.basic;

import java.lang.reflect.Field;

public class InspectObjectClass {
    public static void inspectObject(Object obj) {
        if(obj==null){
            System.out.println("Object is null");
            return;
        }

        Class<?>clazz = obj.getClass();
        Field[]fields = clazz.getDeclaredFields();
        for (Field field:fields){
            field.setAccessible(true);
            try {
                String name = field.getName();
                String type = field.getType().getSimpleName();
                Object value = field.get(obj);
                System.out.println("Field '" + name + "' (" + type + ") = " + value);
            }catch (IllegalAccessException e){
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws NoSuchMethodException {
        inspectObject(new Person("John", 25));
    }
}
