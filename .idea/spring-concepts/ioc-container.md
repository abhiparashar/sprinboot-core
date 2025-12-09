# Topic 3: Spring IoC Container

## Why This Matters for Spring AI Issue #4694
- The warning happens because beans are created in a **specific order**
- Understanding IoC = understanding why bean creation order matters
- `BeanPostProcessor` (the source of warnings) is part of the IoC container

---

## Part 1: What is Inversion of Control (IoC)?

### The Problem Without IoC
```java
// Traditional approach - YOU create dependencies
public class OrderService {
    private PaymentService paymentService;
    private InventoryService inventoryService;
    private EmailService emailService;
    
    public OrderService() {
        // You manually create all dependencies
        this.paymentService = new PaymentService();
        this.inventoryService = new InventoryService();
        this.emailService = new EmailService();
    }
}

// Problems:
// 1. OrderService knows HOW to create PaymentService
// 2. Hard to swap implementations (e.g., for testing)
// 3. If PaymentService constructor changes, OrderService must change
// 4. Tight coupling everywhere
```

### With IoC - Control is Inverted
```java
// IoC approach - SOMEONE ELSE gives you dependencies
public class OrderService {
    private PaymentService paymentService;
    private InventoryService inventoryService;
    private EmailService emailService;
    
    // Dependencies are "injected" from outside
    public OrderService(PaymentService paymentService, 
                        InventoryService inventoryService,
                        EmailService emailService) {
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
        this.emailService = emailService;
    }
}

// Benefits:
// 1. OrderService doesn't know HOW to create dependencies
// 2. Easy to swap implementations
// 3. Loose coupling
// 4. Easy to test with mocks
```

### The "Inversion"
| Traditional | IoC |
|-------------|-----|
| You create objects | Container creates objects |
| You manage lifecycle | Container manages lifecycle |
| You wire dependencies | Container wires dependencies |
| You control the flow | Container controls the flow |

---

## Part 2: What is a Bean?

### Simple Definition
A **bean** is just an object that Spring creates and manages for you.

### What Makes It a Bean?
```java
// This is just a regular Java class
public class UserService {
    public void createUser(String name) { }
}

// It becomes a "bean" when Spring manages it
@Component  // <-- This tells Spring: "manage this class as a bean"
public class UserService {
    public void createUser(String name) { }
}
```

### Bean vs Regular Object
| Regular Object | Spring Bean |
|----------------|-------------|
| Created with `new` | Created by Spring |
| You manage its lifecycle | Spring manages lifecycle |
| You wire dependencies | Spring injects dependencies |
| Garbage collected when no references | Lives in Spring container |

---

## Part 3: ApplicationContext - The IoC Container

### What is ApplicationContext?
It's the **central interface** to Spring's IoC container. Think of it as:
- A factory that creates beans
- A registry that holds all beans
- A manager that handles bean lifecycle

### Creating ApplicationContext
```java
// 1. Annotation-based (most common in Spring Boot)
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

// 2. XML-based (legacy, but still used)
ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");

// 3. Spring Boot auto-creates it for you
@SpringBootApplication
public class MyApp {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(MyApp.class, args);
    }
}
```

### Getting Beans from Context
```java
// By type
UserService userService = context.getBean(UserService.class);

// By name
UserService userService = (UserService) context.getBean("userService");

// By name and type
UserService userService = context.getBean("userService", UserService.class);
```

---

## Part 4: Ways to Define Beans

### Method 1: @Component and Stereotypes
```java
@Component  // Generic component
public class MyComponent { }

@Service    // Business logic layer
public class UserService { }

@Repository // Data access layer
public class UserRepository { }

@Controller // Web layer (MVC)
public class UserController { }

// All of these are @Component underneath!
// They're just semantic hints about the bean's purpose
```

### Method 2: @Bean in @Configuration Class
```java
@Configuration
public class AppConfig {
    
    @Bean
    public UserService userService() {
        return new UserService();
    }
    
    @Bean
    public PaymentService paymentService() {
        return new PaymentService();
    }
    
    // You can use method parameters for dependency injection
    @Bean
    public OrderService orderService(UserService userService, PaymentService paymentService) {
        return new OrderService(userService, paymentService);
    }
}
```

### Method 3: XML Configuration (Legacy)
```xml
<beans>
    <bean id="userService" class="com.example.UserService"/>
    
    <bean id="orderService" class="com.example.OrderService">
        <constructor-arg ref="userService"/>
    </bean>
</beans>
```

### When to Use Which?
| Approach | Use When |
|----------|----------|
| `@Component` | You own the class, simple creation |
| `@Bean` method | Third-party class, complex creation logic |
| XML | Legacy projects, external configuration |

---

## Part 5: Dependency Injection (DI)

### Constructor Injection (Recommended)
```java
@Service
public class OrderService {
    private final UserService userService;
    private final PaymentService paymentService;
    
    // Spring automatically injects dependencies
    public OrderService(UserService userService, PaymentService paymentService) {
        this.userService = userService;
        this.paymentService = paymentService;
    }
}
```

### Field Injection (Convenient but Less Preferred)
```java
@Service
public class OrderService {
    @Autowired
    private UserService userService;
    
    @Autowired
    private PaymentService paymentService;
}
// Problems: harder to test, hides dependencies, can't use final
```

### Setter Injection (For Optional Dependencies)
```java
@Service
public class OrderService {
    private UserService userService;
    
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
```

### Why Constructor Injection is Best
```java
// 1. Dependencies are explicit
// 2. Can use 'final' - immutable
// 3. Easy to test without Spring
// 4. Fails fast if dependency missing

@Service
public class OrderService {
    private final UserService userService;  // final = guaranteed set
    
    public OrderService(UserService userService) {
        this.userService = userService;
    }
}

// Easy to test!
@Test
void testOrderService() {
    UserService mockUserService = mock(UserService.class);
    OrderService orderService = new OrderService(mockUserService);
    // test...
}
```

---

## Part 6: Bean Scopes

### Singleton (Default)
```java
@Component
@Scope("singleton")  // Default, can omit
public class UserService { }

// Only ONE instance for the entire application
// Same object returned every time you request it
```

### Prototype
```java
@Component
@Scope("prototype")
public class ShoppingCart { }

// NEW instance created every time you request it
```

### Web Scopes (Spring MVC)
```java
@Component
@Scope("request")    // New instance per HTTP request
public class RequestLogger { }

@Component
@Scope("session")    // New instance per user session
public class UserPreferences { }
```

### Demonstrating Scopes
```java
// Singleton - same instance
UserService service1 = context.getBean(UserService.class);
UserService service2 = context.getBean(UserService.class);
System.out.println(service1 == service2);  // true

// Prototype - different instances
ShoppingCart cart1 = context.getBean(ShoppingCart.class);
ShoppingCart cart2 = context.getBean(ShoppingCart.class);
System.out.println(cart1 == cart2);  // false
```

---

## Part 7: Component Scanning

### How Spring Finds Your Beans
```java
@SpringBootApplication  // Includes @ComponentScan
public class MyApplication { }

// Spring scans from this package and all sub-packages
// com.example.myapp
// com.example.myapp.service    <- scanned
// com.example.myapp.repository <- scanned
// com.other                    <- NOT scanned
```

### Custom Component Scanning
```java
@Configuration
@ComponentScan(basePackages = {"com.example.service", "com.example.repository"})
public class AppConfig { }

// Or scan specific classes
@ComponentScan(basePackageClasses = {UserService.class, UserRepository.class})
public class AppConfig { }
```

### What Happens During Scanning
```java
// Internally, Spring does something like:

1. Find all classes in specified packages
2. For each class:
   - Use reflection to check: class.isAnnotationPresent(Component.class)?
   - Also check for @Service, @Repository, @Controller (they include @Component)
3. For each @Component class:
   - Create a BeanDefinition
   - Register it in the container
4. Later, create actual bean instances from BeanDefinitions
```

---

## Part 8: BeanDefinition - The Blueprint

### What is BeanDefinition?
It's a **recipe** for creating a bean. Contains:
- Class name
- Scope
- Dependencies
- Initialization method
- **Role** ← Important for Issue #4694!

### BeanDefinition Example
```java
// When Spring sees this:
@Component
@Scope("prototype")
public class UserService { }

// It creates a BeanDefinition like (pseudo-code):
BeanDefinition bd = new BeanDefinition();
bd.setBeanClassName("com.example.UserService");
bd.setScope("prototype");
bd.setRole(BeanDefinition.ROLE_APPLICATION);  // Default
```

### Bean Roles (Key to Issue #4694!)
```java
public interface BeanDefinition {
    // Regular business beans - default
    int ROLE_APPLICATION = 0;
    
    // Supporting infrastructure beans
    int ROLE_SUPPORT = 1;
    
    // Internal Spring infrastructure - SKIP WARNINGS
    int ROLE_INFRASTRUCTURE = 2;
}
```

### Setting Bean Role
```java
@Configuration
public class AppConfig {
    
    // Default role = ROLE_APPLICATION
    @Bean
    public UserService userService() {
        return new UserService();
    }
    
    // Explicitly set as infrastructure
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public SpecialPostProcessor specialPostProcessor() {
        return new SpecialPostProcessor();
    }
}
```

---

## Part 9: Container Hierarchy

### Parent-Child Contexts
```java
// Parent context (shared beans)
ApplicationContext parentContext = new AnnotationConfigApplicationContext(SharedConfig.class);

// Child context (inherits from parent)
AnnotationConfigApplicationContext childContext = new AnnotationConfigApplicationContext();
childContext.setParent(parentContext);
childContext.register(WebConfig.class);
childContext.refresh();

// Child can access parent's beans
// Parent cannot access child's beans
```

### Spring Boot's Context Hierarchy
```
Bootstrap Context (Spring Cloud)
    └── Application Context (Your app)
            └── Servlet Context (Web MVC) [optional]
```

---

## Part 10: Practical Example - Building Mini IoC Container

### Step 1: Simple Container
```java
public class MiniContainer {
    private Map<Class<?>, Object> beans = new HashMap<>();
    
    // Register a bean
    public <T> void register(Class<T> type, T instance) {
        beans.put(type, instance);
    }
    
    // Get a bean
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        return (T) beans.get(type);
    }
}

// Usage
MiniContainer container = new MiniContainer();
container.register(UserService.class, new UserService());

UserService service = container.getBean(UserService.class);
```

### Step 2: Add Annotation Scanning
```java
public class MiniContainer {
    private Map<Class<?>, Object> beans = new HashMap<>();
    
    public void scan(Class<?>... classes) throws Exception {
        for (Class<?> clazz : classes) {
            // Check for @Component
            if (clazz.isAnnotationPresent(Component.class)) {
                // Create instance using reflection
                Object instance = clazz.getDeclaredConstructor().newInstance();
                beans.put(clazz, instance);
                System.out.println("Registered bean: " + clazz.getSimpleName());
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        return (T) beans.get(type);
    }
}

// Usage
@Component
class UserService { }

@Component
class OrderService { }

MiniContainer container = new MiniContainer();
container.scan(UserService.class, OrderService.class);
// Output:
// Registered bean: UserService
// Registered bean: OrderService
```

### Step 3: Add Simple DI
```java
public class MiniContainer {
    private Map<Class<?>, Object> beans = new HashMap<>();
    
    public void scan(Class<?>... classes) throws Exception {
        // First pass: create all beans
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Component.class)) {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                beans.put(clazz, instance);
            }
        }
        
        // Second pass: inject dependencies
        for (Object bean : beans.values()) {
            injectDependencies(bean);
        }
    }
    
    private void injectDependencies(Object bean) throws Exception {
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                Object dependency = beans.get(field.getType());
                if (dependency != null) {
                    field.set(bean, dependency);
                    System.out.println("Injected " + field.getType().getSimpleName() + 
                                       " into " + bean.getClass().getSimpleName());
                }
            }
        }
    }
}
```

---

## Exercises

### Exercise 1: Basic Bean Registration
```java
// Create a simple IoC container that:
// 1. Stores beans by name
// 2. Can retrieve beans by name

public class SimpleContainer {
    // Your implementation
    
    public void registerBean(String name, Object bean) { }
    public Object getBean(String name) { }
}

// Test:
// container.registerBean("userService", new UserService());
// UserService us = (UserService) container.getBean("userService");
```

### Exercise 2: Type-Safe Container
```java
// Extend Exercise 1 to be type-safe

public class TypeSafeContainer {
    public <T> void registerBean(Class<T> type, T bean) { }
    public <T> T getBean(Class<T> type) { }
}

// Test:
// container.registerBean(UserService.class, new UserService());
// UserService us = container.getBean(UserService.class); // No casting!
```

### Exercise 3: Annotation-Based Registration
```java
// Create @ManagedBean annotation
// Container should auto-discover classes with this annotation

@ManagedBean
class UserService { }

public class AnnotationContainer {
    public void register(Class<?>... classes) { }
    public <T> T getBean(Class<T> type) { }
}
```

### Exercise 4: Singleton vs Prototype Scope
```java
// Add scope support to your container

@ManagedBean(scope = "singleton")
class ConfigService { }

@ManagedBean(scope = "prototype")  
class RequestHandler { }

// getBean should return same instance for singleton
// getBean should return new instance for prototype
```

### Exercise 5: Simple Dependency Injection
```java
// Add @Inject support to your container

@ManagedBean
class UserRepository { }

@ManagedBean
class UserService {
    @Inject
    private UserRepository userRepository;
}

// After container.register(), UserService.userRepository should be set
```

---

## Solutions

### Solution 1: Basic Bean Registration
```java
public class SimpleContainer {
    private Map<String, Object> beans = new HashMap<>();
    
    public void registerBean(String name, Object bean) {
        beans.put(name, bean);
    }
    
    public Object getBean(String name) {
        return beans.get(name);
    }
}
```

### Solution 2: Type-Safe Container
```java
public class TypeSafeContainer {
    private Map<Class<?>, Object> beans = new HashMap<>();
    
    public <T> void registerBean(Class<T> type, T bean) {
        beans.put(type, bean);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        return (T) beans.get(type);
    }
}
```

### Solution 3: Annotation-Based Registration
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ManagedBean {
    String scope() default "singleton";
}

public class AnnotationContainer {
    private Map<Class<?>, Object> beans = new HashMap<>();
    
    public void register(Class<?>... classes) throws Exception {
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(ManagedBean.class)) {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                beans.put(clazz, instance);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        return (T) beans.get(type);
    }
}
```

### Solution 4: Singleton vs Prototype Scope
```java
public class ScopedContainer {
    private Map<Class<?>, Object> singletons = new HashMap<>();
    private Set<Class<?>> prototypes = new HashSet<>();
    
    public void register(Class<?>... classes) throws Exception {
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(ManagedBean.class)) {
                ManagedBean annotation = clazz.getAnnotation(ManagedBean.class);
                
                if ("prototype".equals(annotation.scope())) {
                    prototypes.add(clazz);
                } else {
                    // singleton - create once
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    singletons.put(clazz, instance);
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) throws Exception {
        if (singletons.containsKey(type)) {
            return (T) singletons.get(type);
        }
        if (prototypes.contains(type)) {
            return (T) type.getDeclaredConstructor().newInstance();
        }
        return null;
    }
}
```

### Solution 5: Simple Dependency Injection
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Inject { }

public class DIContainer {
    private Map<Class<?>, Object> beans = new HashMap<>();
    
    public void register(Class<?>... classes) throws Exception {
        // First pass: create instances
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(ManagedBean.class)) {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                beans.put(clazz, instance);
            }
        }
        
        // Second pass: inject dependencies
        for (Object bean : beans.values()) {
            for (Field field : bean.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Object dependency = beans.get(field.getType());
                    if (dependency != null) {
                        field.set(bean, dependency);
                    }
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        return (T) beans.get(type);
    }
}
```

---

## Key Takeaways for Issue #4694

1. **ApplicationContext is the IoC container** - it creates and manages all beans
2. **BeanDefinition is the blueprint** - contains metadata including `role`
3. **Bean role determines warning behavior** - `ROLE_INFRASTRUCTURE` skips warnings
4. **Beans are created in a specific order** - some must be created early (BeanPostProcessors)
5. **Component scanning uses reflection** - Spring reads annotations at runtime

---

## Next Topic
→ [04-spring-bean-lifecycle.md](./04-spring-bean-lifecycle.md) - Complete bean lifecycle flow