package com.wiredi.runtime.time;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.List;

import static java.time.temporal.ChronoUnit.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TimedTest {

    private static List<Arguments> toStringEntries() {
        Duration days = Duration.of(3, DAYS);
        Duration hours = Duration.of(4, HOURS);
        Duration minutes = Duration.of(7, MINUTES);
        Duration seconds = Duration.of(100, SECONDS);
        Duration nanos = Duration.of(25, NANOS);
        Duration micros = Duration.of(100, MICROS);

        return List.of(
                arguments(
                        Timed.of(days)
                                .plus(hours)
                                .plus(minutes)
                                .plus(seconds)
                                .plus(nanos)
                                .plus(micros),
                        "3d, 4h, 8m, 40s"
                ),
                arguments(
                        Timed.of(hours)
                                .plus(minutes)
                                .plus(seconds)
                                .plus(nanos)
                                .plus(micros),
                        "4h, 8m, 40s"
                ),
                arguments(
                        Timed.of(minutes)
                                .plus(seconds)
                                .plus(nanos)
                                .plus(micros),
                        "8m, 40s"
                ),
                arguments(
                        Timed.of(seconds)
                                .plus(nanos)
                                .plus(micros),
                        "1m, 40s"
                ),
                arguments(
                        Timed.of(nanos),
                        "25ns"
                ),
                arguments(
                        Timed.of(nanos).plus(micros),
                        "100㎲, 25ns"
                ),
                arguments(
                        Timed.of(micros),
                        "100㎲"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("toStringEntries")
    public void test(Timed timed, String render) {
        assertThat(timed.toString()).isEqualTo(render);
    }
}
