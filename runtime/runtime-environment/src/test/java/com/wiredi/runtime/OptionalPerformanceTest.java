package com.wiredi.runtime;

import com.wiredi.runtime.properties.TypeMapper;
import com.wiredi.runtime.time.Timed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Properties;

public class OptionalPerformanceTest {

    @Test
    public void test() {
        TestProperties properties = new TestProperties();
        long repetitions = 1000;
        long runs = 100;
        Timed allOptional = Timed.ZERO;
        Timed allNullable = Timed.ZERO;


        for (int i = 0; i < repetitions; i++) {
            properties.set(i);
        }

        for (int run = 0; run < runs; run++) {
            System.out.println("Run " + run + ":");
            Timed optional = Timed.of(() -> {
                for (int i = 0; i < repetitions; i++) {
                    properties.tryGet(i);
                }
            });

            Timed nullable = Timed.of(() -> {
                for (int i = 0; i < repetitions; i++) {
                    properties.getNullable(i);
                }
            });

            System.out.println("Optional: " + optional);
            allOptional = allOptional.plus(optional);
            System.out.println("Nullable: " + nullable);
            allNullable = allNullable.plus(nullable);
            System.out.println();
        }

        Timed averageOptional = allOptional.dividedBy(runs);
        Timed averageNullable = allNullable.dividedBy(runs);
        System.out.println("Averages: ");
        System.out.println("Average optional: " + averageOptional);
        System.out.println("Average nullable: " + averageNullable);
        if(averageOptional.isGreaterThan(averageNullable)) {
            System.out.println("####");
            System.out.println("Nullable was, on average, faster than Optional by: " + averageOptional.minus(averageNullable));
            System.out.println("####");
        } else {
            System.out.println("####");
            System.out.println("Nullable was, on average, faster than Optional by: " + averageNullable.minus(averageOptional));
            System.out.println("####");
        }
    }


    class TestProperties {

        private final Properties properties = new Properties();
        private final TypeMapper typeMapper = TypeMapper.newPreconfigured();

        public void set(int value) {
            properties.put("test-" + value, Integer.toString(value));
        }

        @Nullable
        public Integer getNullable(int key) {
            String s = properties.getProperty("test-" + key);
            if (s != null) {
                return typeMapper.parse(int.class, s);
            } else {
                return null;
            }
        }

        @NotNull
        public Optional<Integer> tryGet(int key) {
            return Optional.ofNullable(properties.getProperty("test-" + key))
                    .map(value -> typeMapper.parse(int.class, value));
        }
    }
}
