package com.example.prac.reflectionPrac.inspectingClasses.InspectingPrivateMembers;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AnnotationFinder {

    public static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Method> annotatedMethods = new ArrayList<>();

        // Get all declared methods
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            // Check if method has the specified annotation
            if (method.isAnnotationPresent(annotation)) {
                annotatedMethods.add(method);
            }
        }

        return annotatedMethods;
    }

    public static void main(String[] args) {
        // Test with @Deprecated annotation
        System.out.println("=== Methods with @Deprecated ===");
        List<Method> deprecatedMethods = findAnnotatedMethods(SampleService.class, Deprecated.class);
        for (Method m : deprecatedMethods) {
            System.out.println("- " + m.getName());
        }

        // Test with custom @Important annotation
        System.out.println("\n=== Methods with @Important ===");
        List<Method> importantMethods = findAnnotatedMethods(SampleService.class, Important.class);
        for (Method m : importantMethods) {
            System.out.println("- " + m.getName());
        }

        // Test with custom @Loggable annotation
        System.out.println("\n=== Methods with @Loggable ===");
        List<Method> loggableMethods = findAnnotatedMethods(SampleService.class, Loggable.class);
        for (Method m : loggableMethods) {
            System.out.println("- " + m.getName());
        }
    }
}

// Custom annotations
@Retention(RetentionPolicy.RUNTIME)
@interface Important {}

@Retention(RetentionPolicy.RUNTIME)
@interface Loggable {}

// Sample class with various annotations
class SampleService {

    @Important
    public void processOrder() {
        System.out.println("Processing order...");
    }

    @Deprecated
    public void oldMethod() {
        System.out.println("This is old");
    }

    @Important
    @Loggable
    public void saveData() {
        System.out.println("Saving data...");
    }

    @Loggable
    public void fetchData() {
        System.out.println("Fetching data...");
    }

    public void regularMethod() {
        System.out.println("Just a regular method");
    }

    @Deprecated
    @Loggable
    public void legacyExport() {
        System.out.println("Legacy export");
    }
}