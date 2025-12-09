# Topic 2: Java Annotations

## Why This Matters for Spring AI Issue #4694
- `@McpTool` is an annotation that Spring scans for
- `@Role(BeanDefinition.ROLE_INFRASTRUCTURE)` is the **fix** we need to apply
- Understanding how annotations work = understanding the fix

---

## Part 1: What are Annotations?

### Simple Definition
Annotations are **metadata** — extra information attached to code that doesn't change what the code does, but tells tools/frameworks how to treat it.

### Real-World Analogy
Think of sticky notes on documents:
- The document (code) stays the same
- The sticky note (annotation) tells someone reading it "Handle this specially"

### Basic Syntax
```java
@AnnotationName
public class MyClass { }

@AnnotationName(value = "something")
public void myMethod() { }

@AnnotationName(name = "test", count = 5)
private String myField;
```

---

## Part 2: Built-in Java Annotations

### Common Ones You've Seen
```java
@Override  // Tells compiler: this method overrides parent
public String toString() { return "..."; }

@Deprecated  // Tells developers: don't use this anymore
public void oldMethod() { }

@SuppressWarnings("unchecked")  // Tells compiler: ignore this warning
public void riskyMethod() { }

@FunctionalInterface  // Tells compiler: this interface should have exactly 1 abstract method
public interface MyFunction { void apply(); }
```

---

## Part 3: Creating Custom Annotations

### Simplest Annotation
```java
public @interface MyAnnotation {
    // Empty annotation - just a marker
}

// Usage
@MyAnnotation
public class MyClass { }
```

### Annotation with Elements (Parameters)
```java
public @interface Author {
    String name();
    String date();
    int version() default 1;  // default value - optional when using
}

// Usage
@Author(name = "John", date = "2024-01-15")  // version defaults to 1
public class MyClass { }

@Author(name = "Jane", date = "2024-02-20", version = 2)
public class AnotherClass { }
```

### Special 'value' Element
```java
public @interface Priority {
    int value();  // When only one element named 'value', can skip name
}

// Both are equivalent:
@Priority(value = 5)
@Priority(5)  // Shorthand when element is named 'value'
```

---

## Part 4: Meta-Annotations (Annotations on Annotations)

These control how your annotation behaves. **Critical for understanding Spring annotations!**

### @Retention - How Long Does It Live?
```java
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// SOURCE - Discarded by compiler, only in source code
// Used for: IDE hints, compile-time checks
@Retention(RetentionPolicy.SOURCE)
public @interface Todo { String value(); }

// CLASS - In .class file but NOT available at runtime (default)
// Used for: Bytecode tools
@Retention(RetentionPolicy.CLASS)
public @interface BytecodeHint { }

// RUNTIME - Available at runtime via reflection ⭐ MOST IMPORTANT FOR SPRING
// Used for: Frameworks like Spring that read annotations at runtime
@Retention(RetentionPolicy.RUNTIME)
public @interface McpTool { String name(); }
```

### @Target - Where Can It Be Used?
```java
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

// Only on classes
@Target(ElementType.TYPE)
public @interface Entity { }

// Only on methods
@Target(ElementType.METHOD)
public @interface GetMapping { }

// Only on fields
@Target(ElementType.FIELD)
public @interface Autowired { }

// Multiple targets
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Transactional { }

// All possible targets:
// TYPE - class, interface, enum
// FIELD - fields
// METHOD - methods
// PARAMETER - method parameters
// CONSTRUCTOR - constructors
// LOCAL_VARIABLE - local variables
// ANNOTATION_TYPE - other annotations
// PACKAGE - package-info.java
// TYPE_PARAMETER - generic type parameters (Java 8+)
// TYPE_USE - any type use (Java 8+)
```

### @Documented - Include in Javadoc?
```java
import java.lang.annotation.Documented;

@Documented
public @interface PublicApi { }
// Now @PublicApi will appear in generated Javadoc
```

### @Inherited - Do Subclasses Get It?
```java
import java.lang.annotation.Inherited;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited { }

@Audited
public class ParentClass { }

public class ChildClass extends ParentClass { }
// ChildClass is also @Audited because of @Inherited
```

---

## Part 5: Reading Annotations at Runtime (Using Reflection)

### Checking if Annotation Exists
```java
@MyAnnotation
public class MyClass { }

// Check class annotation
Class<?> clazz = MyClass.class;
boolean hasAnnotation = clazz.isAnnotationPresent(MyAnnotation.class);
System.out.println("Has @MyAnnotation: " + hasAnnotation);  // true
```

### Getting Annotation Instance
```java
@Author(name = "John", date = "2024-01-15", version = 2)
public class MyClass { }

Class<?> clazz = MyClass.class;
Author author = clazz.getAnnotation(Author.class);

if (author != null) {
    System.out.println("Author: " + author.name());    // John
    System.out.println("Date: " + author.date());      // 2024-01-15
    System.out.println("Version: " + author.version()); // 2
}
```

### Getting Method Annotations
```java
public class Calculator {
    @Deprecated
    @Author(name = "Old Dev", date = "2020-01-01")
    public int oldAdd(int a, int b) { return a + b; }
}

Method method = Calculator.class.getMethod("oldAdd", int.class, int.class);

// Get all annotations
Annotation[] annotations = method.getAnnotations();
for (Annotation a : annotations) {
    System.out.println(a.annotationType().getSimpleName());
}
// Output: Deprecated, Author

// Get specific annotation
Author author = method.getAnnotation(Author.class);
```

### Getting Parameter Annotations
```java
public class UserService {
    public void createUser(@NotNull String name, @Min(18) int age) { }
}

Method method = UserService.class.getMethod("createUser", String.class, int.class);
Annotation[][] paramAnnotations = method.getParameterAnnotations();

// paramAnnotations[0] = annotations on 'name' parameter
// paramAnnotations[1] = annotations on 'age' parameter
```

---

## Part 6: Spring's Annotation Model

### How Spring Annotations Are Built
```java
// Simplified version of Spring's @Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {
    String value() default "";
}

// @Service is just @Component with a different name
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component  // <-- @Service IS a @Component
public @interface Service {
    String value() default "";
}
```

### This is How @McpTool Likely Works
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpTool {
    String name() default "";
    String description() default "";
}
```

---

## Part 7: The @Role Annotation (Key to Issue #4694!)

### What is @Role?
```java
// Spring's @Role annotation
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Role {
    int value();
}
```

### BeanDefinition Role Constants
```java
public interface BeanDefinition {
    // Normal application beans - default
    int ROLE_APPLICATION = 0;
    
    // Support beans - infrastructure helpers
    int ROLE_SUPPORT = 1;
    
    // Infrastructure beans - internal Spring use ⭐ THE FIX
    int ROLE_INFRASTRUCTURE = 2;
}
```

### How @Role is Used (The Fix!)
```java
import org.springframework.context.annotation.Role;
import org.springframework.beans.factory.config.BeanDefinition;

@Configuration
public class McpServerAutoConfiguration {
    
    // WITHOUT @Role - Spring warns about BeanPostProcessor ordering
    @Bean
    public McpAnnotatedBeans serverAnnotatedBeanRegistry() {
        return new McpAnnotatedBeans();
    }
    
    // WITH @Role - Spring knows this is infrastructure, no warning ✅
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public McpAnnotatedBeans serverAnnotatedBeanRegistry() {
        return new McpAnnotatedBeans();
    }
}
```

### Why Does @Role Fix the Warning?
```java
// Inside Spring's BeanPostProcessorChecker (simplified):

public Object postProcessAfterInitialization(Object bean, String beanName) {
    // Get bean's role
    int role = getBeanDefinition(beanName).getRole();
    
    // If it's infrastructure, don't warn
    if (role == BeanDefinition.ROLE_INFRASTRUCTURE) {
        return bean;  // No warning!
    }
    
    // Otherwise, if not all BeanPostProcessors are ready, warn
    if (beanPostProcessorsNotFullyInitialized()) {
        logger.warn("Bean '" + beanName + "' is not eligible for...");
    }
    
    return bean;
}
```

---

## Part 8: Annotation Processing at Compile Time (Bonus)

### Annotation Processors
```java
// Processors that run during compilation
@SupportedAnnotationTypes("com.example.Generate")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class MyProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, 
                          RoundEnvironment roundEnv) {
        // Generate code at compile time
        // Lombok uses this!
        return true;
    }
}
```

### Examples of Compile-Time Processing
- **Lombok**: `@Data`, `@Getter`, `@Setter` generate code at compile time
- **MapStruct**: Generates mapper implementations
- **Dagger**: Generates dependency injection code

---

## Part 9: Complete Example - Building a Mini Framework

### Step 1: Define Our Annotation
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleEndpoint {
    String path();
    String method() default "GET";
}
```

### Step 2: Use the Annotation
```java
public class UserController {
    
    @SimpleEndpoint(path = "/users", method = "GET")
    public String getAllUsers() {
        return "List of users";
    }
    
    @SimpleEndpoint(path = "/users", method = "POST")
    public String createUser() {
        return "User created";
    }
}
```

### Step 3: Framework Reads Annotations
```java
public class SimpleFramework {
    private Map<String, Method> routeMap = new HashMap<>();
    
    public void registerController(Object controller) {
        Class<?> clazz = controller.getClass();
        
        for (Method method : clazz.getDeclaredMethods()) {
            // Check if method has our annotation
            if (method.isAnnotationPresent(SimpleEndpoint.class)) {
                SimpleEndpoint endpoint = method.getAnnotation(SimpleEndpoint.class);
                
                // Create route key like "GET:/users"
                String routeKey = endpoint.method() + ":" + endpoint.path();
                
                // Store for later invocation
                routeMap.put(routeKey, method);
                
                System.out.println("Registered: " + routeKey + " -> " + method.getName());
            }
        }
    }
    
    public String handleRequest(String httpMethod, String path, Object controller) throws Exception {
        String routeKey = httpMethod + ":" + path;
        Method method = routeMap.get(routeKey);
        
        if (method != null) {
            return (String) method.invoke(controller);
        }
        return "404 Not Found";
    }
}

// Usage
public class Main {
    public static void main(String[] args) throws Exception {
        SimpleFramework framework = new SimpleFramework();
        UserController controller = new UserController();
        
        framework.registerController(controller);
        // Output:
        // Registered: GET:/users -> getAllUsers
        // Registered: POST:/users -> createUser
        
        String response = framework.handleRequest("GET", "/users", controller);
        System.out.println(response);  // List of users
    }
}
```

---

## Exercises

### Exercise 1: Create a @Loggable Annotation
Create an annotation that marks methods for logging.
```java
// Create the annotation
// Should work on methods only
// Should be available at runtime
// Should have optional 'level' element with default "INFO"

@Loggable(level = "DEBUG")
public void myMethod() { }
```

### Exercise 2: Annotation Reader
Write code that finds all `@Loggable` methods in a class and prints their details.
```java
public static void findLoggableMethods(Class<?> clazz) {
    // Your code here
    // Should print: "Method: myMethod, Level: DEBUG"
}
```

### Exercise 3: Create @NotNull Validator
```java
// Create @NotNull annotation for fields

public class User {
    @NotNull
    private String name;
    
    private String nickname;  // Can be null
}

// Create validator
public static List<String> validate(Object obj) {
    // Return list of field names that are null but shouldn't be
}
```

### Exercise 4: Annotation Inheritance Test
```java
// Create @Tracked annotation with @Inherited

@Tracked
public class Parent { }

public class Child extends Parent { }

// Write code to verify Child has @Tracked
```

### Exercise 5: Simulate Spring's @Role Check (Directly Related to Issue!)
```java
public @interface Role {
    int value();
}

public class InfrastructureBean { }

// Annotate a class with @Role(2) which is ROLE_INFRASTRUCTURE
// Write code that checks if a class should skip BeanPostProcessor warning
public static boolean shouldSkipWarning(Class<?> clazz) {
    // Return true if class has @Role(2)
}
```

---

## Solutions

### Solution 1: @Loggable Annotation
```java
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Loggable {
    String level() default "INFO";
}
```

### Solution 2: Annotation Reader
```java
public static void findLoggableMethods(Class<?> clazz) {
    for (Method method : clazz.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Loggable.class)) {
            Loggable loggable = method.getAnnotation(Loggable.class);
            System.out.println("Method: " + method.getName() + ", Level: " + loggable.level());
        }
    }
}
```

### Solution 3: @NotNull Validator
```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotNull { }

public static List<String> validate(Object obj) {
    List<String> violations = new ArrayList<>();
    
    for (Field field : obj.getClass().getDeclaredFields()) {
        if (field.isAnnotationPresent(NotNull.class)) {
            field.setAccessible(true);
            try {
                if (field.get(obj) == null) {
                    violations.add(field.getName());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    return violations;
}
```

### Solution 4: Annotation Inheritance Test
```java
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Tracked { }

@Tracked
class Parent { }

class Child extends Parent { }

// Test
public static void main(String[] args) {
    boolean parentTracked = Parent.class.isAnnotationPresent(Tracked.class);
    boolean childTracked = Child.class.isAnnotationPresent(Tracked.class);
    
    System.out.println("Parent has @Tracked: " + parentTracked);  // true
    System.out.println("Child has @Tracked: " + childTracked);    // true (inherited!)
}
```

### Solution 5: @Role Check (Issue #4694 Related)
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Role {
    int value();
}

// Constants like Spring's BeanDefinition
class BeanDefinition {
    static final int ROLE_APPLICATION = 0;
    static final int ROLE_SUPPORT = 1;
    static final int ROLE_INFRASTRUCTURE = 2;
}

@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
class InfrastructureBean { }

@Role(BeanDefinition.ROLE_APPLICATION)
class ApplicationBean { }

class RegularBean { }  // No @Role

public static boolean shouldSkipWarning(Class<?> clazz) {
    if (!clazz.isAnnotationPresent(Role.class)) {
        return false;  // No @Role = don't skip, show warning
    }
    
    Role role = clazz.getAnnotation(Role.class);
    return role.value() == BeanDefinition.ROLE_INFRASTRUCTURE;
}

// Test
public static void main(String[] args) {
    System.out.println("InfrastructureBean skip: " + shouldSkipWarning(InfrastructureBean.class)); // true
    System.out.println("ApplicationBean skip: " + shouldSkipWarning(ApplicationBean.class));       // false
    System.out.println("RegularBean skip: " + shouldSkipWarning(RegularBean.class));               // false
}
```

---

## Key Takeaways for Issue #4694

1. **`@McpTool` is a runtime annotation** that Spring scans using reflection
2. **`@Role(BeanDefinition.ROLE_INFRASTRUCTURE)`** tells Spring "this bean is infrastructure, don't warn"
3. **The fix is adding `@Role`** to configuration classes and bean methods that create early-initialized beans
4. **Meta-annotations like `@Retention(RUNTIME)`** make it possible for Spring to read annotations at runtime

---

## Next Topic
→ [03-spring-ioc-container.md](./03-spring-ioc-container.md) - How Spring manages objects