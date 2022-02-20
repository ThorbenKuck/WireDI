package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.domain.IdentifiableProvider;

import java.util.List;
import java.util.stream.Collectors;

public enum WireConflictStrategy {
    BEST_MATCH {
        @Override
        public <T> IdentifiableProvider<T> find(List<IdentifiableProvider<T>> identifiableProviders, Class<T> expectedType) {
            List<IdentifiableProvider<T>> bestMatches = identifiableProviders.stream()
                    .sorted((o1, o2) -> {
                        boolean firstDirectlyMatches = expectedType.equals(o1.type());
                        boolean secondDirectlyMatches = expectedType.equals(o2.type());
                        int compare = Boolean.compare(firstDirectlyMatches, secondDirectlyMatches);

                        if (compare == 0) {
                            return o1.compareTo(o2);
                        } else {
                            return compare;
                        }
                    })
                    .collect(Collectors.toList());

            if(bestMatches.isEmpty()) {
                error(identifiableProviders, 0, expectedType);
            }

            return bestMatches.get(0);
        }
    },
    FIRST_DIRECT_MATCH {
        @Override
        public <T> IdentifiableProvider<T> find(List<IdentifiableProvider<T>> identifiableProviders, Class<T> expectedType) {
            return identifiableProviders.stream()
                    .filter(it -> it.type().equals(expectedType))
                    .findFirst()
                    .orElseGet(() -> error(identifiableProviders, 0, expectedType));
        }
    },
    DIRECT_MATCH {
        @Override
        public <T> IdentifiableProvider<T> find(List<IdentifiableProvider<T>> identifiableProviders, Class<T> expectedType) {
            List<IdentifiableProvider<T>> directMatches = identifiableProviders.stream()
                    .filter(it -> it.type().equals(expectedType))
                    .collect(Collectors.toList());

            if (directMatches.size() != 1) {
                error(identifiableProviders, directMatches.size(), expectedType);
            }

            return directMatches.get(0);
        }
    },
    NONE {
        @Override
        public <T> IdentifiableProvider<T> find(List<IdentifiableProvider<T>> identifiableProviders, Class<T> expectedType) {
            return error(identifiableProviders, identifiableProviders.size(), expectedType);
        }
    },
    FIRST {
        @Override
        public <T> IdentifiableProvider<T> find(List<IdentifiableProvider<T>> identifiableProviders, Class<T> expectedType) {
            return identifiableProviders.stream()
                    .findFirst()
                    .orElseGet(() -> error(identifiableProviders, 0, expectedType));
        }
    };

    public static WireConflictStrategy DEFAULT = DIRECT_MATCH;

    public abstract <T> IdentifiableProvider<T> find(List<IdentifiableProvider<T>> providerList, Class<T> expectedType);

    public <T>IdentifiableProvider<T> error(List<IdentifiableProvider<T>> total, int match, Class<T> type) throws DiInstantiationException {
        StringBuilder result = new StringBuilder();
        result.append("Expected to find exactly 1 Provider for type ").append(type).append(" using the ").append(name()).append(" strategy, but got ").append(match).append(" out of ").append(total.size()).append(" potential candidates")
                .append(System.lineSeparator())
        .append("Candidates: ").append(System.lineSeparator());

        total.forEach(provider -> result.append(" - ").append(provider.toString()).append(System.lineSeparator()));
        throw new DiInstantiationException(result.toString());
    }
}
