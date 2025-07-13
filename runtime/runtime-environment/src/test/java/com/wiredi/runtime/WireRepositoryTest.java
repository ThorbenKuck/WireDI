package com.wiredi.runtime;

import com.wiredi.runtime.domain.errors.ExceptionHandler;
import com.wiredi.runtime.domain.errors.results.ExceptionHandlingResult;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.IdentifiableProviderSource;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.infrastructure.GenericBase;
import com.wiredi.runtime.lang.Counter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class WireRepositoryTest {

    @Nested
    class Registrations {
        private static List<Arguments> argumentsList() {
            List<Arguments> returnValue = new ArrayList<>();
            Map<Class<?>, Object> values = new HashMap<>();
            values.put(String.class, IdentifiableProvider.singleton("Test", String.class));
            values.put(char.class, IdentifiableProvider.singleton('a', char.class));
            values.put(boolean.class, IdentifiableProvider.singleton(true, boolean.class));
            values.put(int.class, IdentifiableProvider.singleton(1, int.class));
            values.put(long.class, IdentifiableProvider.singleton(1L, long.class));
            values.put(float.class, IdentifiableProvider.singleton(0.0f, float.class));
            values.put(double.class, IdentifiableProvider.singleton(0.0d, double.class));
            values.put(List.class, IdentifiableProvider.singleton(List.of("1"), List.class));
            values.put(Set.class, IdentifiableProvider.singleton(Set.of("1"), Set.class));
            values.put(Collection.class, IdentifiableProvider.singleton(List.of("1"), Collection.class));

            values.forEach((correctKey, value) -> values.keySet().forEach(key -> {
                returnValue.add(arguments(key, value, key.equals(correctKey)));
            }));

            return returnValue;
        }

        @ParameterizedTest
        @MethodSource("argumentsList")
        public void test(Class<Object> type, IdentifiableProvider<?> provider, boolean find) {
            // Arrange
            WireContainer wireContainer = WireContainer.create();
            wireContainer.initializer().setSources(IdentifiableProviderSource.just(provider));
            wireContainer.load();

            // Act
            Optional<Object> result = wireContainer.tryGet(type);

            // Assert
            assertThat(result.isPresent()).isEqualTo(find);
            if (find) {
                assertThat(result).contains(provider.get(wireContainer));
            }
        }
    }

    @Nested
    class MultiGenericTestClasses {

        private WireContainer repository;


        @BeforeEach
        void setup() {
            repository = WireContainer.create();
            repository.initializer().setSources(List.of(IdentifiableProviderSource.just(
                    new MultiGenericClass<>(new ImplementationA(), new ImplementationB()).getProvider(),
                    new MultiGenericClass<>(new ImplementationAA(), new ImplementationB()).getProvider(),
                    new MultiGenericClass<>(new ImplementationA(), new ImplementationBB()).getProvider(),
                    new MultiGenericClass<>(new ImplementationAA(), new ImplementationBB()).getProvider()
            )));
            repository.load();
        }

        @Test
        void searchingWithoutGenericsFindsEverything() {
            assertThat(repository.getAll(TypeIdentifier.of(MultiGenericClass.class)))
                    .containsExactlyInAnyOrder(
                            new MultiGenericClass<>(new ImplementationA(), new ImplementationB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationB()),
                            new MultiGenericClass<>(new ImplementationA(), new ImplementationBB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationBB())
                    );
        }

        @Test
        void searchingForObjectFindsEverything() {
            assertThat(repository.getAll(TypeIdentifier.of(MultiGenericClass.class).withGeneric(Object.class).withGeneric(Object.class)))
                    .containsExactlyInAnyOrder(
                            new MultiGenericClass<>(new ImplementationA(), new ImplementationB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationB()),
                            new MultiGenericClass<>(new ImplementationA(), new ImplementationBB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationBB())
                    );

        }

        @Test
        void searchingForInterfaceAndInterfaceClassFindsEverything() {
            assertThat(repository.getAll(TypeIdentifier.of(MultiGenericClass.class).withGeneric(Interface.class).withGeneric(Interface.class)))
                    .containsExactlyInAnyOrder(
                            new MultiGenericClass<>(new ImplementationA(), new ImplementationB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationB()),
                            new MultiGenericClass<>(new ImplementationA(), new ImplementationBB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationBB())
                    );
        }

        @Test
        void searchingForAAndInterfaceBaseClassFindsEverything() {
            assertThat(repository.getAll(TypeIdentifier.of(MultiGenericClass.class).withGeneric(ImplementationA.class).withGeneric(Interface.class)))
                    .containsExactlyInAnyOrder(
                            new MultiGenericClass<>(new ImplementationA(), new ImplementationB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationB()),
                            new MultiGenericClass<>(new ImplementationA(), new ImplementationBB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationBB())
                    );
        }

        @Test
        void searchingForAAAndInterfaceBaseClassFindsEverything() {
            assertThat(repository.getAll(TypeIdentifier.of(MultiGenericClass.class).withGeneric(ImplementationAA.class).withGeneric(Interface.class)))
                    .containsExactlyInAnyOrder(
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationBB())
                    );
        }

        @Test
        void searchingForInterfaceAndBClassFindsEverything() {
            assertThat(repository.getAll(TypeIdentifier.of(MultiGenericClass.class).withGeneric(Interface.class).withGeneric(ImplementationB.class)))
                    .containsExactlyInAnyOrder(
                            new MultiGenericClass<>(new ImplementationA(), new ImplementationB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationB()),
                            new MultiGenericClass<>(new ImplementationA(), new ImplementationBB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationBB())
                    );
        }

        @Test
        void searchingForAAndBBaseClassFindsEverything() {
            assertThat(repository.getAll(TypeIdentifier.of(MultiGenericClass.class).withGeneric(ImplementationA.class).withGeneric(ImplementationB.class)))
                    .containsExactlyInAnyOrder(
                            new MultiGenericClass<>(new ImplementationA(), new ImplementationB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationB()),
                            new MultiGenericClass<>(new ImplementationA(), new ImplementationBB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationBB())
                    );
        }

        @Test
        void searchingForAAAndBBaseClassFindsEverything() {
            assertThat(repository.getAll(TypeIdentifier.of(MultiGenericClass.class).withGeneric(ImplementationAA.class).withGeneric(ImplementationB.class)))
                    .containsExactlyInAnyOrder(
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationBB())
                    );
        }

        @Test
        void searchingForInterfaceAndBBClassFindsEverything() {
            assertThat(repository.getAll(TypeIdentifier.of(MultiGenericClass.class).withGeneric(Interface.class).withGeneric(ImplementationBB.class)))
                    .containsExactlyInAnyOrder(
                            new MultiGenericClass<>(new ImplementationA(), new ImplementationBB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationBB())
                    );
        }

        @Test
        void searchingForAAndBBBaseClassFindsEverything() {
            assertThat(repository.getAll(TypeIdentifier.of(MultiGenericClass.class).withGeneric(ImplementationA.class).withGeneric(ImplementationBB.class)))
                    .containsExactlyInAnyOrder(
                            new MultiGenericClass<>(new ImplementationA(), new ImplementationBB()),
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationBB())
                    );
        }

        @Test
        void searchingForAAAndBBBaseClassFindsEverything() {
            assertThat(repository.getAll(TypeIdentifier.of(MultiGenericClass.class).withGeneric(ImplementationAA.class).withGeneric(ImplementationBB.class)))
                    .containsExactlyInAnyOrder(
                            new MultiGenericClass<>(new ImplementationAA(), new ImplementationBB())
                    );
        }

        private interface Interface {
        }

        private class ImplementationA implements Interface {
        }

        private class ImplementationB implements Interface {
        }

        private class ImplementationAA extends ImplementationA {
        }

        private class ImplementationBB extends ImplementationB {
        }

        private class MultiGenericClass<T extends Interface, S extends Interface> {
            private final T t;
            private final S s;

            private MultiGenericClass(T t, S s) {
                this.t = t;
                this.s = s;
            }

            public T getT() {
                return t;
            }

            public S getS() {
                return s;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof MultiGenericClass<?, ?> that)) return false;
                return Objects.equals(t.getClass(), that.t.getClass()) && Objects.equals(s.getClass(), that.s.getClass());
            }

            @Override
            public int hashCode() {
                return Objects.hash(t.getClass(), s.getClass());
            }

            @Override
            public String toString() {
                return "MultiGenericClass<" +
                        t.getClass().getSimpleName() +
                        "," +
                        s.getClass().getSimpleName() +
                        '>';
            }

            public IdentifiableProvider<MultiGenericClass<T, S>> getProvider() {
                MultiGenericClass<T, S> it = this;
                return new IdentifiableProvider<>() {
                    @Override
                    public @NotNull TypeIdentifier<? super MultiGenericClass<T, S>> type() {
                        return TypeIdentifier.of(MultiGenericClass.class)
                                .withGeneric(t.getClass())
                                .withGeneric(s.getClass());
                    }

                    @Override
                    public @NotNull List<TypeIdentifier<?>> additionalWireTypes() {
                        return List.of(
                                TypeIdentifier.of(MultiGenericClass.class)
                                        .withGeneric(t.getClass())
                                        .withGeneric(s.getClass())
                        );
                    }

                    @Override
                    public @Nullable MultiGenericClass<T, S> get(@NotNull WireContainer wireContainer, @NotNull TypeIdentifier<MultiGenericClass<T, S>> concreteType) {
                        return it;
                    }
                };
            }
        }
    }

    @Nested
    class SingleGenericTestClasses {

        private WireContainer repository;

        @BeforeEach
        void setup() {
            repository = WireContainer.create();
            repository.initializer().setSources(List.of(
                    IdentifiableProviderSource.just(
                            GenericBase.provider("Test1"),
                            GenericBase.provider("Test2"),
                            GenericBase.provider(1),
                            GenericBase.provider(2),
                            GenericBase.provider(true),
                            GenericBase.provider(false)
                    )
            ));
            repository.load();
        }

        @Test
        void searchingWithoutAGeneric() {
            Collection<GenericBase> all = repository.getAll(TypeIdentifier.of(GenericBase.class));
            assertThat(all).containsExactlyInAnyOrder(
                    GenericBase.of("Test1"),
                    GenericBase.of("Test2"),
                    GenericBase.of(1),
                    GenericBase.of(2),
                    GenericBase.of(true),
                    GenericBase.of(false)
            );
        }

        @Test
        void searchingWithAParentClassGeneric() {
            Collection<GenericBase<Object>> all = repository.getAll(TypeIdentifier.of(GenericBase.class).withGeneric(Object.class));
            assertThat(all).containsExactlyInAnyOrder(
                    GenericBase.of("Test1"),
                    GenericBase.of("Test2"),
                    GenericBase.of(1),
                    GenericBase.of(2),
                    GenericBase.of(true),
                    GenericBase.of(false)
            );
        }

        @Test
        void searchingForTheStringGeneric() {
            Collection<GenericBase<String>> all = repository.getAll(TypeIdentifier.of(GenericBase.class).withGeneric(String.class));
            assertThat(all).containsExactlyInAnyOrder(
                    GenericBase.of("Test1"),
                    GenericBase.of("Test2")
            );
        }

        @Test
        void searchingForTheIntGeneric() {
            Collection<GenericBase<Integer>> all = repository.getAll(TypeIdentifier.of(GenericBase.class).withGeneric(int.class));
            assertThat(all).containsExactlyInAnyOrder(
                    GenericBase.of(1),
                    GenericBase.of(2)
            );
        }

        @Test
        void searchingForTheBooleanGeneric() {
            Collection<GenericBase<Boolean>> all = repository.getAll(TypeIdentifier.of(GenericBase.class).withGeneric(boolean.class));
            assertThat(all).containsExactlyInAnyOrder(
                    GenericBase.of(true),
                    GenericBase.of(false)
            );
        }
    }

    @Nested
    class ErrorHandlingTest {

        private final IllegalArgumentErrorHandler illegalArgumentErrorHandler = new IllegalArgumentErrorHandler();
        private final IllegalStateErrorHandler illegalStateErrorHandler = new IllegalStateErrorHandler();

        @BeforeEach
        public void beforeEach() {
            illegalArgumentErrorHandler.invocations.reset();
            illegalStateErrorHandler.invocations.reset();
        }

        @Test
        public void handlingASpecificExceptionIsPossible() {
            // Arrange
            WireContainer wireContainer = WireContainer.create();
            wireContainer.announce(IdentifiableProvider.singleton(illegalArgumentErrorHandler, TypeIdentifier.of(ExceptionHandler.class).withGeneric(IllegalArgumentException.class)));
            wireContainer.announce(IdentifiableProvider.singleton(illegalStateErrorHandler, TypeIdentifier.of(ExceptionHandler.class).withGeneric(IllegalStateException.class)));
            wireContainer.load();

            // Act
            try {
                wireContainer.exceptionHandler().handle(new IllegalArgumentException());
            } catch (Throwable e) {
                fail("Error handling did not work correctly", e);
            }

            // Assert
            assertThat(illegalArgumentErrorHandler.invocations.get()).isEqualTo(1);
            assertThat(illegalStateErrorHandler.invocations.get()).isEqualTo(0);

        }

        @Test
        public void handlingASpecificSecondExceptionIsPossible() {
            // Arrange
            WireContainer wireContainer = WireContainer.create();
            wireContainer.announce(IdentifiableProvider.singleton(illegalArgumentErrorHandler, TypeIdentifier.of(ExceptionHandler.class).withGeneric(IllegalArgumentException.class)));
            wireContainer.announce(IdentifiableProvider.singleton(illegalStateErrorHandler, TypeIdentifier.of(ExceptionHandler.class).withGeneric(IllegalStateException.class)));
            wireContainer.load();

            // Act
            try {
                wireContainer.exceptionHandler().handle(new IllegalStateException("This is an IllegalStateException"));
                fail("IllegalStateException should have been rethrown");
            } catch (AssertionError t) {
                throw t;
            } catch (Throwable ignored) {
            }

            // Assert
            assertThat(illegalArgumentErrorHandler.invocations.get()).isEqualTo(0);
            assertThat(illegalStateErrorHandler.invocations.get()).isEqualTo(1);

        }

        class IllegalArgumentErrorHandler implements ExceptionHandler<IllegalArgumentException> {

            private final Counter invocations = new Counter(0);

            @Override
            public @NotNull ExceptionHandlingResult<IllegalArgumentException> handle(@NotNull IllegalArgumentException error) {
                invocations.increment();
                return ExceptionHandlingResult.doNothing();
            }
        }

        class IllegalStateErrorHandler implements ExceptionHandler<IllegalStateException> {

            private final Counter invocations = new Counter(0);

            @Override
            public @NotNull ExceptionHandlingResult<IllegalStateException> handle(@NotNull IllegalStateException error) {
                invocations.increment();
                return ExceptionHandlingResult.rethrow(error);
            }
        }
    }
}
