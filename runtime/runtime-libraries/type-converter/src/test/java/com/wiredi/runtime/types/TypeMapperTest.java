package com.wiredi.runtime.types;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TypeMapperTest {

    private static <T> Arguments argument(Object input, T t, Class<T> type) {
        return Arguments.of(input, t, type);
    }

    public static List<Arguments> argumentsList() {
        short one = 1;
        short minusTen = -10;
        return List.of(
                argument("Test", "Test".getBytes(), byte[].class),

                argument("Test".getBytes(), "Test", String.class),
                argument("Test", "Test", String.class),

                argument("A", TestEnum.A, TestEnum.class),
                argument("A".getBytes(), TestEnum.A, TestEnum.class),

                argument(Bytes.convert(1), 1, int.class),
                argument("1", 1, int.class),
                argument("-10", -10, int.class),
                argument(1, 1, int.class),
                argument(1L, 1, int.class),
                argument(1F, 1, int.class),
                argument(1D, 1, int.class),

                argument(Bytes.convert(one), one, short.class),
                argument("1", one, short.class),
                argument("-10", minusTen, short.class),
                argument(1, one, short.class),
                argument(1L, one, short.class),
                argument(1F, one, short.class),
                argument(1D, one, short.class),

                argument(Bytes.convert(1L), 1L, long.class),
                argument("1", 1L, long.class),
                argument("-10", -10L, long.class),
                argument(1, 1L, long.class),
                argument(1L, 1L, long.class),
                argument(1F, 1L, long.class),
                argument(1D, 1L, long.class),

                argument(Bytes.convert(1.0d), 1.0d, double.class),
                argument("1", 1.0d, double.class),
                argument("1.0", 1.0d, double.class),
                argument(1, 1.0d, double.class),
                argument(1L, 1.0d, double.class),
                argument(1F, 1.0d, double.class),
                argument(1D, 1.0d, double.class),

                argument(Bytes.convert(1.0f), 1.0f, float.class),
                argument("1", 1.0f, float.class),
                argument("1.0", 1.0f, float.class),
                argument(1, 1.0f, float.class),
                argument(1L, 1.0f, float.class),
                argument(1F, 1.0f, float.class),
                argument(1D, 1.0f, float.class)
        );
    }

    @ParameterizedTest
    @MethodSource("argumentsList")
    public void typeMapperCanConvertBytesToString(Object input, Object expected, Class<?> targetType) {
        // Arrange
        TypeMapper typeMapper = TypeMapper.newPreconfigured();

        // Act
        Object actual = typeMapper.convert(input, targetType);

        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    enum TestEnum {
        A, B, C
    }
}