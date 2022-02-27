package com.github.thorbenkuck.di.aspects;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AspectInstanceWireRepositoryTest {

    private final TestClass testClass = new TestClass();
    private final TestAnnotation testAnnotation = testClass.getClass().getAnnotation(TestAnnotation.class);
    private final SecondTestAnnotation secondTestAnnotation = testClass.getClass().getAnnotation(SecondTestAnnotation.class);

    @Test
    public void verifyThatTheAspectRepositoryWorksWithTwoAnnotationAndNoArguments() {
        // Arrange
        AspectRepository aspectRepository = new AspectRepository();
        AtomicInteger result = new AtomicInteger(0);
        aspectRepository.registerFor(TestAnnotation.class, context -> {
            result.addAndGet(1);
            return context.proceed();
        });
        aspectRepository.registerFor(TestAnnotation.class, context -> {
            result.addAndGet(2);
            return context.proceed();
        });
        aspectRepository.registerFor(TestAnnotation.class, context -> {
            result.addAndGet(3);
            return context.proceed();
        });
        aspectRepository.registerFor(SecondTestAnnotation.class, context -> {
            result.addAndGet(4);
            return context.proceed();
        });


        Optional<AspectWrapper<TestAnnotation>> access = aspectRepository.access(TestAnnotation.class);
        Optional<AspectWrapper<SecondTestAnnotation>> access2 = aspectRepository.access(SecondTestAnnotation.class);
        assertTrue(access.isPresent());
        assertTrue(access2.isPresent());

        ExecutionContext<TestAnnotation> tempContext = new ExecutionContext<>(access.get(), testAnnotation, c -> {
            result.addAndGet(5);
            return null;
        });

        ExecutionContext<SecondTestAnnotation> context2 = new ExecutionContext<>(access2.get(), secondTestAnnotation, tempContext);

        // Act
        context2.run();

        // Assert
        assertThat(result.get()).isEqualTo(1 + 2 + 3 + 4 + 5);
    }

    @Test
    public void verifyThatTheAspectRepositoryWorksWithOneAnnotationAndNoArguments() {
        // Arrange
        AspectRepository aspectRepository = new AspectRepository();
        AtomicInteger result = new AtomicInteger(0);
        aspectRepository.registerFor(TestAnnotation.class, context -> {
            result.addAndGet(1);
            return context.proceed();
        });
        aspectRepository.registerFor(TestAnnotation.class, context -> {
            result.addAndGet(2);
            return context.proceed();
        });
        aspectRepository.registerFor(TestAnnotation.class, context -> {
            result.addAndGet(3);
            return context.proceed();
        });
        Optional<AspectWrapper<TestAnnotation>> access = aspectRepository.access(TestAnnotation.class);
        assertTrue(access.isPresent());

        // Act
        ExecutionContext<TestAnnotation> context = new ExecutionContext<>(access.get(), testAnnotation, c -> {
            result.addAndGet(4);
            return null;
        });

        // Assert
        context.run();
        assertThat(result.get()).isEqualTo(1 + 2 + 3 + 4);
    }

    @Test
    public void verifyThatTheAspectRepositoryWorksWithOneAnnotationAndOneArgument() {
        // Arrange
        AspectRepository aspectRepository = new AspectRepository();
        AtomicInteger result = new AtomicInteger(0);
        aspectRepository.registerFor(TestAnnotation.class, context -> {
            result.addAndGet(1);
            return context.proceed();
        });
        aspectRepository.registerFor(TestAnnotation.class, context -> {
            result.addAndGet(2);
            return context.proceed();
        });
        aspectRepository.registerFor(TestAnnotation.class, context -> {
            result.addAndGet(3);
            return context.proceed();
        });
        Optional<AspectWrapper<TestAnnotation>> access = aspectRepository.access(TestAnnotation.class);
        assertTrue(access.isPresent());

        // Act
        ExecutionContext<TestAnnotation> context = new ExecutionContext<>(access.get(), testAnnotation, c -> {
            int toAdd = assertDoesNotThrow(() -> c.requireArgumentAs("value", Integer.class));
            result.addAndGet(toAdd);
            return null;
        });

        // Assert
        context.setArgument("value", 10);
        context.run();
        assertThat(result.get()).isEqualTo(1 + 2 + 3 + 10);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {}

    @Retention(RetentionPolicy.RUNTIME)
    @interface SecondTestAnnotation {}

    @TestAnnotation
    @SecondTestAnnotation
    static class TestClass {}
}