package com.example.prac.reflectionPrac.inspectingClasses.InspectingPrivateMembers;

import com.example.prac.reflectionPrac.inspectingClasses.BankAccount;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InspectPrivate {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // Reading Private Fields
        BankAccount account  = new BankAccount("ACC-001",500.00);
        Field balanceField = BankAccount.class.getDeclaredField("balance");
        balanceField.setAccessible(true); // Bypass private access
        double balance = (double) balanceField.get(account);
        System.out.println("Balance: " + balance);

        // Modifying Private Fields
        balanceField.setAccessible(true);
        balanceField.set(account,99999.0);
        System.out.println(account.getBalance());

        // Invoking Private Methods
        Method method = BankAccount.class.getDeclaredMethod("auditLog",String.class);
        method.setAccessible(true);
        method.invoke(account,"Suspicious activity");
    }
}
