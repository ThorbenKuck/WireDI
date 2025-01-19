package com.wiredi.runtime.messaging.compression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Algorithm {

    final List<String> identifiers;

    private Algorithm(
            List<String> identifiers
    ) {
        this.identifiers = identifiers;
    }

    public static Algorithm just(String algorithm) {
        return new Algorithm(List.of(algorithm));
    }

    public static Algorithm inOrder(String first, String... others) {
        List<String> result = new ArrayList<>();
        result.add(first);
        result.addAll(Arrays.asList(others));
        return new Algorithm(result);
    }

    public static Algorithm inOrder(Collection<String> algorithms) {
        return new Algorithm(new ArrayList<>(algorithms));
    }
}
