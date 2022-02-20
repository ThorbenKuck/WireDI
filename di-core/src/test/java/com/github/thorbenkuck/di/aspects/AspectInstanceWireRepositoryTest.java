package com.github.thorbenkuck.di.aspects;

import com.sun.org.apache.bcel.internal.generic.DCONST;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AspectInstanceWireRepositoryTest {

    @Test
    public void test() {
        // Arrange
        AspectRepository aspectRepository = new AspectRepository();
        aspectRepository.registerFor(TestAnnotation.class, context -> {
            System.out.println("3");
            return context.proceed();
        });
        aspectRepository.registerFor(TestAnnotation.class, context -> {
            System.out.println("2");
            return context.proceed();
        });
        aspectRepository.registerFor(TestAnnotation.class, context -> {
            System.out.println("1");
            return context.proceed();
        });
        aspectRepository.registerFor(SecondTestAnnotation.class, context -> {
            String foo = context.requireArgumentAs("foo", String.class);
            System.out.println("SOMETHING SPECIAL!");
            return context.proceed();
        });
        TestClass testClass = new TestClass();
        TestAnnotation annotation = testClass.getClass().getAnnotation(TestAnnotation.class);
        SecondTestAnnotation annotation2 = testClass.getClass().getAnnotation(SecondTestAnnotation.class);

        // Act
        Optional<AspectWrapper<TestAnnotation>> access = aspectRepository.access(TestAnnotation.class);
        Optional<AspectWrapper<SecondTestAnnotation>> access2 = aspectRepository.access(SecondTestAnnotation.class);
        assertTrue(access.isPresent());
        assertTrue(access2.isPresent());

        AspectWrapper<TestAnnotation> testAnnotationAspectWrapper = access.get();
        AspectWrapper<SecondTestAnnotation> secondTestAnnotationAspectWrapper = access2.get();

        ExecutionContext<TestAnnotation> context = new ExecutionContext<>(testAnnotationAspectWrapper, annotation, c -> {
            System.out.println("4");
            return null;
        });

        ExecutionContext<TestAnnotation> tempContext = new ExecutionContext<>(testAnnotationAspectWrapper, annotation, c -> {
            System.out.println("4");
            return null;
        });

        ExecutionContext<SecondTestAnnotation> context2 = new ExecutionContext<>(secondTestAnnotationAspectWrapper, annotation2, tempContext);

        System.out.println("## First ##");
        context.run();

        System.out.println("\n## Second ##");
        context2.setArgument("foo", "foo");
        context2.run();

        // Assert
        context.clear();
        context2.clear();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {}

    @Retention(RetentionPolicy.RUNTIME)
    @interface SecondTestAnnotation {}

    @TestAnnotation
    @SecondTestAnnotation
    static class TestClass { }
}