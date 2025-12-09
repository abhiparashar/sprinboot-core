# Topic 1: Java Reflection Basics

## Why This Matters for Spring AI Issue #4694
Spring framework heavily uses reflection to:
- Scan for annotations like `@McpTool`
- Create bean instances dynamically
- Invoke methods without knowing them at compile time

Understanding reflection = understanding how Spring "sees" your code at runtime.

---

## Part 1: What is Reflection?

### Simple Definition
Reflection is Java's ability to **inspect and manipulate classes, methods, fields at runtime** — even if you don't know their names at compile time.

### Real-World Analogy
Imagine you're a detective investigating a house (a Java class):
- Normal Java: You have the house blueprint, you know every room
- Reflection: You enter a house you've never seen, but you can still find all rooms, open all drawers, read all documents

### Why Does It Exist?
```java
// Without reflection - you must know the class at compile time
User user = new User();
user.setName("John");

// With reflection - you can work with ANY class
Object obj = Class.forName("com.example.User").newInstance();
Method setter = obj.getClass().getMethod("setName", String.class);
setter.invoke(obj, "John");
```

---

## Part 2: Core Reflection Classes

### The Big Four
| Class | Purpose | Example Use |
|-------|---------|-------------|
| `Class<?>` | Represents a class itself | Get class name, methods, fields |
| `Method` | Represents a method | Invoke methods dynamically |
| `Field` | Represents a field/variable | Read/write private fields |
| `Constructor` | Represents a constructor | Create instances dynamically |

### Getting a Class Object (3 Ways)
```java
// Way 1: From an instance
String text = "Hello";
Class<?> clazz1 = text.getClass();

// Way 2: From the class literal
Class<?> clazz2 = String.class;

// Way 3: From fully qualified name (most flexible)
Class<?> clazz3 = Class.forName("java.lang.String");
```

---

## Part 3: Inspecting Classes

### Example Class We'll Inspect
```java
package com.example;

public class BankAccount {
    private String accountNumber;
    private double balance;
    public String ownerName;
    
    public BankAccount() {}
    
    public BankAccount(String accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }
    
    public void deposit(double amount) {
        this.balance += amount;
    }
    
    private void auditLog(String action) {
        System.out.println("Audit: " + action);
    }
    
    public double getBalance() {
        return balance;
    }
}
```

### Inspecting Fields
```java
Class<?> clazz = BankAccount.class;

// Get ALL declared fields (including private)
Field[] allFields = clazz.getDeclaredFields();
for (Field f : allFields) {
    System.out.println("Field: " + f.getName() + ", Type: " + f.getType().getSimpleName());
}
// Output:
// Field: accountNumber, Type: String
// Field: balance, Type: double
// Field: ownerName, Type: String

// Get only PUBLIC fields (including inherited)
Field[] publicFields = clazz.getFields();
// Only returns: ownerName
```

### Inspecting Methods
```java
Class<?> clazz = BankAccount.class;

// All declared methods (including private, excluding inherited)
Method[] allMethods = clazz.getDeclaredMethods();
for (Method m : allMethods) {
    System.out.println("Method: " + m.getName() + ", Returns: " + m.getReturnType().getSimpleName());
}
// Output:
// Method: deposit, Returns: void
// Method: auditLog, Returns: void
// Method: getBalance, Returns: double

// Get specific method by name and parameter types
Method depositMethod = clazz.getMethod("deposit", double.class);
```

### Inspecting Constructors
```java
Class<?> clazz = BankAccount.class;

Constructor<?>[] constructors = clazz.getDeclaredConstructors();
for (Constructor<?> c : constructors) {
    System.out.println("Constructor params: " + c.getParameterCount());
}
// Output:
// Constructor params: 0
// Constructor params: 2
```

---

## Part 4: Creating Instances Dynamically

### Using No-Arg Constructor
```java
Class<?> clazz = BankAccount.class;

// Modern way (Java 9+)
Constructor<?> constructor = clazz.getDeclaredConstructor();
Object account = constructor.newInstance();
```

### Using Parameterized Constructor
```java
Class<?> clazz = BankAccount.class;

Constructor<?> constructor = clazz.getDeclaredConstructor(String.class, double.class);
Object account = constructor.newInstance("ACC-001", 1000.0);
```

### This is How Spring Creates Your Beans!
```java
// When Spring sees @Component on a class, internally it does something like:
String className = "com.example.BankAccount"; // from scanning
Class<?> beanClass = Class.forName(className);
Constructor<?> constructor = beanClass.getDeclaredConstructor();
Object bean = constructor.newInstance();
// Then it registers this as a bean
```

---

## Part 5: Accessing Private Members

### Reading Private Fields
```java
BankAccount account = new BankAccount("ACC-001", 500.0);

Field balanceField = BankAccount.class.getDeclaredField("balance");
balanceField.setAccessible(true);  // Bypass private access
double balance = (double) balanceField.get(account);
System.out.println("Balance: " + balance);  // 500.0
```

### Modifying Private Fields
```java
BankAccount account = new BankAccount("ACC-001", 500.0);

Field balanceField = BankAccount.class.getDeclaredField("balance");
balanceField.setAccessible(true);
balanceField.set(account, 99999.0);  // Hacking the balance!

System.out.println(account.getBalance());  // 99999.0
```

### Invoking Private Methods
```java
BankAccount account = new BankAccount("ACC-001", 500.0);

Method auditMethod = BankAccount.class.getDeclaredMethod("auditLog", String.class);
auditMethod.setAccessible(true);
auditMethod.invoke(account, "Suspicious activity");
// Output: Audit: Suspicious activity
```

### Why setAccessible(true)?
- Java has access modifiers (private, protected, public)
- `setAccessible(true)` tells JVM: "I know what I'm doing, let me access it"
- Spring uses this extensively to inject dependencies into private fields

---

## Part 6: Checking Modifiers

```java
import java.lang.reflect.Modifier;

Class<?> clazz = BankAccount.class;

for (Field f : clazz.getDeclaredFields()) {
    int modifiers = f.getModifiers();
    
    System.out.println(f.getName() + ":");
    System.out.println("  Is private? " + Modifier.isPrivate(modifiers));
    System.out.println("  Is public? " + Modifier.isPublic(modifiers));
    System.out.println("  Is static? " + Modifier.isStatic(modifiers));
    System.out.println("  Is final? " + Modifier.isFinal(modifiers));
}
```

---

## Part 7: How Spring Uses Reflection (Preview)

### Scenario: Spring Scanning for @McpTool
```java
// Simplified version of what Spring does

public void scanForMcpTools(String packageName) {
    // 1. Find all classes in package (using classpath scanning)
    List<Class<?>> classes = findClassesInPackage(packageName);
    
    // 2. For each class, check methods for @McpTool
    for (Class<?> clazz : classes) {
        for (Method method : clazz.getDeclaredMethods()) {
            // 3. Reflection to check annotation
            if (method.isAnnotationPresent(McpTool.class)) {
                // 4. Get annotation details
                McpTool annotation = method.getAnnotation(McpTool.class);
                String toolName = annotation.name();
                
                // 5. Register this method as an MCP tool
                registerTool(toolName, method);
            }
        }
    }
}
```

---

## Part 8: Performance Considerations

### Reflection is Slower
```java
// Direct call - FAST (nanoseconds)
account.deposit(100);

// Reflection call - SLOWER (microseconds)
Method m = BankAccount.class.getMethod("deposit", double.class);
m.invoke(account, 100);
```

### Why Spring Still Uses It
1. Flexibility > Raw speed for initialization
2. Reflection happens mostly at startup, not during requests
3. Spring caches reflection metadata to avoid repeated lookups

---

## Exercises

### Exercise 1: Class Inspector
Create a method that takes any object and prints all its fields with their current values.
```java
public static void inspectObject(Object obj) {
    // Your code here
    // Should print:
    // Field 'name' (String) = John
    // Field 'age' (int) = 25
}

// Test with:
// inspectObject(new Person("John", 25));
```

### Exercise 2: Universal Setter
Create a method that sets any field on any object by field name.
```java
public static void setField(Object obj, String fieldName, Object value) {
    // Your code here
}

// Test with:
// Person p = new Person();
// setField(p, "name", "Alice");
// setField(p, "age", 30);
```

### Exercise 3: Method Caller
Create a method that calls any method by name with given arguments.
```java
public static Object callMethod(Object obj, String methodName, Object... args) {
    // Your code here
}

// Test with:
// Calculator calc = new Calculator();
// Object result = callMethod(calc, "add", 5, 3);  // returns 8
```

### Exercise 4: Simple Bean Factory
Create a factory that creates instances of any class by name.
```java
public class SimpleBeanFactory {
    public static Object createBean(String className) {
        // Your code here
    }
}

// Test with:
// Object list = SimpleBeanFactory.createBean("java.util.ArrayList");
```

### Exercise 5: Annotation Finder (Important for Issue #4694!)
Create a method that finds all methods in a class that have a specific annotation.
```java
public static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
    // Your code here
}

// Test with custom annotation or use @Deprecated:
// List<Method> methods = findAnnotatedMethods(SomeClass.class, Deprecated.class);
```

---

## Solutions

### Solution 1: Class Inspector
```java
public static void inspectObject(Object obj) {
    Class<?> clazz = obj.getClass();
    Field[] fields = clazz.getDeclaredFields();
    
    for (Field field : fields) {
        field.setAccessible(true);
        try {
            Object value = field.get(obj);
            System.out.println("Field '" + field.getName() + "' (" + 
                field.getType().getSimpleName() + ") = " + value);
        } catch (IllegalAccessException e) {
            System.out.println("Field '" + field.getName() + "' - Cannot access");
        }
    }
}
```

### Solution 2: Universal Setter
```java
public static void setField(Object obj, String fieldName, Object value) {
    try {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new RuntimeException("Failed to set field: " + fieldName, e);
    }
}
```

### Solution 3: Method Caller
```java
public static Object callMethod(Object obj, String methodName, Object... args) {
    try {
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
            // Handle primitives
            if (paramTypes[i] == Integer.class) paramTypes[i] = int.class;
            if (paramTypes[i] == Double.class) paramTypes[i] = double.class;
        }
        
        Method method = obj.getClass().getMethod(methodName, paramTypes);
        return method.invoke(obj, args);
    } catch (Exception e) {
        throw new RuntimeException("Failed to call method: " + methodName, e);
    }
}
```

### Solution 4: Simple Bean Factory
```java
public class SimpleBeanFactory {
    public static Object createBean(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + className, e);
        }
    }
}
```

### Solution 5: Annotation Finder
```java
public static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
    List<Method> result = new ArrayList<>();
    
    for (Method method : clazz.getDeclaredMethods()) {
        if (method.isAnnotationPresent(annotation)) {
            result.add(method);
        }
    }
    
    return result;
}
```

---

## Key Takeaways for Issue #4694

1. **Spring uses reflection to find `@McpTool` annotated methods**
2. **Spring creates beans dynamically using `Constructor.newInstance()`**
3. **Spring reads annotations using `method.getAnnotation()`**
4. **`setAccessible(true)` allows Spring to work with private members**

---

## Next Topic
→ [02-java-annotations.md](./02-java-annotations.md) - How annotations work internally