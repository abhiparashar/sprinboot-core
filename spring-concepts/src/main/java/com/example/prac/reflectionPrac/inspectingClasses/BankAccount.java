package com.example.prac.reflectionPrac.inspectingClasses;

public class BankAccount {
    private String accountNumber;
    private double balance;
    public String ownerName;

    public BankAccount() {}

    public BankAccount(String accountNumber, double balance){
        this.balance = balance;
        this.accountNumber = accountNumber;
    }

    public void deposit(double amount){
        this.balance += amount;
    }

    private void auditLog(String action) {
        System.out.println("Audit: " + action);
    }

    public double getBalance() {
        return balance;
    }
}
