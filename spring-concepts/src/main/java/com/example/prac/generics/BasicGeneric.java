package com.example.prac.generics;

class Box<T>{
    private T item;
    public void put(T item){
        this.item = item;
    }

    public T get(){
        return item;
    }

}
public class BasicGeneric {
    public static void main(String[] args) {
        // Usage
        Box<String> stringBox = new Box<>();
        stringBox.put("Hello");
        String value = stringBox.get();
        System.out.println(value);

        Box<Integer> intBox = new Box<>();
        intBox.put(42);
        Integer number = intBox.get();
        System.out.println(number);
    }
}
