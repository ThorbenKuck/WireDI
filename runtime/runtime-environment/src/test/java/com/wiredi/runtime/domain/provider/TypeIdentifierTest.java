package com.wiredi.runtime.domain.provider;

import com.google.common.collect.Streams;
import com.google.common.primitives.Primitives;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.wiredi.runtime.lang.Preconditions.is;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TypeIdentifierTest {

    public static List<PrimitiveWithWrapper> allPrimitives() {
        return Primitives.allPrimitiveTypes()
                .stream()
                .map(primitive -> new PrimitiveWithWrapper(primitive, Primitives.wrap(primitive)))
                .toList();
    }

    @Test
    public void assignable() {
        TypeIdentifier<Collection<List>> generalCollection = TypeIdentifier.of(Collection.class).withGeneric(List.class);
        TypeIdentifier<List> generalList = TypeIdentifier.of(List.class);

        assertThat(generalCollection.isAssignableFrom(generalList)).isTrue();
        assertThat(generalList.isAssignableFrom(generalList)).isTrue();
    }

    @Test
    public void instanceOf() {
        TypeIdentifier<Collection<Collection>> generalCollection = TypeIdentifier.of(Collection.class).withGeneric(Collection.class);
        TypeIdentifier<List<List>> generalList = TypeIdentifier.of(List.class).withGeneric(List.class);

        assertThat(generalList.isInstanceOf(generalCollection)).isTrue();
    }

    @Test
    public void instanceOfLessGenerics() {
        TypeIdentifier<Collection<List>> generalCollection = TypeIdentifier.of(Collection.class).withGeneric(List.class);
        TypeIdentifier<List> generalList = TypeIdentifier.of(List.class);

        assertThat(generalList.isInstanceOf(generalCollection)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("equalArguments")
    public void testThatEqualTypeIdentifiersAreEqual(TypeIdentifier<?> base, TypeIdentifier<?> specification) {
        assertThat(base).isEqualTo(specification);
    }

    @ParameterizedTest
    @MethodSource("notEqualArguments")
    public void testThatNotEqualTypeIdentifiersAreNotEqual(TypeIdentifier<?> base, TypeIdentifier<?> specification) {
        assertThat(base).isNotEqualTo(specification);
    }

    public Stream<Arguments> equalArguments() {
        return Streams.concat(
                Stream.of(
                        arguments(TypeIdentifier.of(List.class), TypeIdentifier.of(List.class).withGeneric(TypeIdentifier.of(TypeIdentifier.class).withGeneric(String.class))),
                        arguments(TypeIdentifier.of(List.class), TypeIdentifier.of(List.class).withGeneric(Integer.class)),
                        arguments(TypeIdentifier.of(List.class).withGeneric(String.class), TypeIdentifier.of(List.class).withGeneric(String.class)),
                        arguments(TypeIdentifier.of(String.class), TypeIdentifier.of(String.class))
                ),
                allPrimitives().stream().flatMap(wrapper -> Stream.of(
                                arguments(TypeIdentifier.of(wrapper.wrapperType), TypeIdentifier.of(wrapper.primitiveType)),
                                arguments(TypeIdentifier.of(wrapper.primitiveType), TypeIdentifier.of(wrapper.wrapperType))
                        )
                ),

                allPrimitives().stream().flatMap(wrapper -> Stream.of(
                                arguments(TypeIdentifier.of(List.class).withGeneric(wrapper.wrapperType), TypeIdentifier.of(List.class).withGeneric(wrapper.primitiveType)),
                                arguments(TypeIdentifier.of(List.class).withGeneric(wrapper.primitiveType), TypeIdentifier.of(List.class).withGeneric(wrapper.wrapperType)),
                                arguments(TypeIdentifier.of(List.class).withGeneric(TypeIdentifier.of(wrapper.wrapperType)), TypeIdentifier.of(List.class).withGeneric(TypeIdentifier.of(wrapper.primitiveType))),
                                arguments(TypeIdentifier.of(List.class).withGeneric(TypeIdentifier.of(wrapper.primitiveType)), TypeIdentifier.of(List.class).withGeneric(TypeIdentifier.of(wrapper.wrapperType)))
                        )
                ),
                allPrimitives().stream().flatMap(wrapper -> Stream.of(
                                arguments(TypeIdentifier.of(Collection.class).withGeneric(wrapper.wrapperType), TypeIdentifier.of(Collection.class).withGeneric(wrapper.primitiveType)),
                                arguments(TypeIdentifier.of(Collection.class).withGeneric(wrapper.primitiveType), TypeIdentifier.of(Collection.class).withGeneric(wrapper.wrapperType)),
                                arguments(TypeIdentifier.of(Collection.class).withGeneric(TypeIdentifier.of(wrapper.wrapperType)), TypeIdentifier.of(Collection.class).withGeneric(TypeIdentifier.of(wrapper.primitiveType))),
                                arguments(TypeIdentifier.of(Collection.class).withGeneric(TypeIdentifier.of(wrapper.primitiveType)), TypeIdentifier.of(Collection.class).withGeneric(TypeIdentifier.of(wrapper.wrapperType)))
                        )
                ),
                allPrimitives().stream().flatMap(wrapper -> Stream.of(
                                arguments(TypeIdentifier.of(Set.class).withGeneric(wrapper.wrapperType), TypeIdentifier.of(Set.class).withGeneric(wrapper.primitiveType)),
                                arguments(TypeIdentifier.of(Set.class).withGeneric(wrapper.primitiveType), TypeIdentifier.of(Set.class).withGeneric(wrapper.wrapperType)),
                                arguments(TypeIdentifier.of(Set.class).withGeneric(TypeIdentifier.of(wrapper.wrapperType)), TypeIdentifier.of(Set.class).withGeneric(TypeIdentifier.of(wrapper.primitiveType))),
                                arguments(TypeIdentifier.of(Set.class).withGeneric(TypeIdentifier.of(wrapper.primitiveType)), TypeIdentifier.of(Set.class).withGeneric(TypeIdentifier.of(wrapper.wrapperType)))
                        )
                )
        );
    }

    public Stream<Arguments> notEqualArguments() {
        return Streams.concat(
                Stream.of(
                        arguments(TypeIdentifier.of(List.class).withGeneric(Integer.class), TypeIdentifier.of(List.class)),
                        arguments(TypeIdentifier.of(List.class).withGeneric(String.class), TypeIdentifier.of(List.class).withGeneric(Integer.class)),
                        arguments(TypeIdentifier.of(String.class), TypeIdentifier.of(Integer.class))
                ),
                allPrimitives().stream().flatMap(wrapper -> Stream.of(
                                arguments(TypeIdentifier.just(wrapper.wrapperType), TypeIdentifier.just(wrapper.primitiveType)),
                                arguments(TypeIdentifier.just(wrapper.primitiveType), TypeIdentifier.just(wrapper.wrapperType))
                        )
                ),
                allPrimitives().stream().flatMap(wrapper -> Stream.of(
                                arguments(TypeIdentifier.of(List.class).withGeneric(TypeIdentifier.just(wrapper.wrapperType)), TypeIdentifier.of(List.class).withGeneric(TypeIdentifier.just(wrapper.primitiveType))),
                                arguments(TypeIdentifier.of(List.class).withGeneric(TypeIdentifier.just(wrapper.primitiveType)), TypeIdentifier.of(List.class).withGeneric(TypeIdentifier.just(wrapper.wrapperType)))
                        )
                ),
                allPrimitives().stream().flatMap(wrapper -> Stream.of(
                                arguments(TypeIdentifier.of(Collection.class).withGeneric(TypeIdentifier.just(wrapper.wrapperType)), TypeIdentifier.of(Collection.class).withGeneric(TypeIdentifier.just(wrapper.primitiveType))),
                                arguments(TypeIdentifier.of(Collection.class).withGeneric(TypeIdentifier.just(wrapper.primitiveType)), TypeIdentifier.of(Collection.class).withGeneric(TypeIdentifier.just(wrapper.wrapperType)))
                        )
                ),
                allPrimitives().stream().flatMap(wrapper -> Stream.of(
                                arguments(TypeIdentifier.of(Set.class).withGeneric(TypeIdentifier.just(wrapper.wrapperType)), TypeIdentifier.of(Set.class).withGeneric(TypeIdentifier.just(wrapper.primitiveType))),
                                arguments(TypeIdentifier.of(Set.class).withGeneric(TypeIdentifier.just(wrapper.primitiveType)), TypeIdentifier.of(Set.class).withGeneric(TypeIdentifier.just(wrapper.wrapperType)))
                        )
                )
        );
    }

    record PrimitiveWithWrapper(Class<?> primitiveType, Class<?> wrapperType) {
        PrimitiveWithWrapper(Class<?> primitiveType, Class<?> wrapperType) {
            this.primitiveType = primitiveType;
            this.wrapperType = wrapperType;
            is(primitiveType.isPrimitive(), () -> "The primitive type must be primitive");
            is(Primitives.isWrapperType(wrapperType), () -> "The wrapper type must be a wrapper");
        }
    }
}