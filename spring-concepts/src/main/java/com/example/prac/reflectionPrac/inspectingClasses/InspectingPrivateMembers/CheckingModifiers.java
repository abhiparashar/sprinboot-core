package com.example.prac.reflectionPrac.inspectingClasses.InspectingPrivateMembers;

import com.example.prac.reflectionPrac.inspectingClasses.BankAccount;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class CheckingModifiers {
    public static void main(String[] args) {
       Class<?>clazz = BankAccount.class;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields){
            int modifier = field.getModifiers();
            System.out.println(field.getName() + ": ");
            System.out.println("  Is private?" + Modifier.isPrivate(modifier));
            System.out.println("  Is public?" + Modifier.isPublic(modifier));
            System.out.println("  Is static?" + Modifier.isStatic(modifier));
            System.out.println("  Is final?" + Modifier.isFinal(modifier));
        }
    }
}
