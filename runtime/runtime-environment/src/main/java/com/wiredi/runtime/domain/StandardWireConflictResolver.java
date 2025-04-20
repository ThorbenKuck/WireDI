package com.wiredi.runtime.domain;

import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.lang.Ordered;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum StandardWireConflictResolver implements WireConflictResolver {
    BEST_MATCH {
        @Override
        @NotNull
        public <T> IdentifiableProvider<T> find(
                @NotNull final List<IdentifiableProvider<T>> identifiableProviders,
                @NotNull final TypeIdentifier<T> expectedType
        ) {
            final List<IdentifiableProvider<T>> bestMatches = identifiableProviders.stream()
                    .sorted((o1, o2) -> {
                        final boolean firstDirectlyMatches = expectedType.equals(o1.type());
                        final boolean secondDirectlyMatches = expectedType.equals(o2.type());
                        final int compare = Boolean.compare(firstDirectlyMatches, secondDirectlyMatches);

                        if (compare == 0) {
                            return Ordered.compare(o1, o2);
                        } else {
                            return compare;
                        }
                    })
                    .toList();

            if (bestMatches.isEmpty()) {
                error(identifiableProviders, 0, expectedType);
            }

            return bestMatches.getFirst();
        }
    },
    FIRST_DIRECT_MATCH {
        @Override
        @NotNull
        public <T> IdentifiableProvider<T> find(
                @NotNull final List<IdentifiableProvider<T>> identifiableProviders,
                @NotNull final TypeIdentifier<T> expectedType
        ) {
            return identifiableProviders.stream()
                    .filter(it -> it.type().equals(expectedType))
                    .sorted()
                    .findFirst()
                    .orElseGet(() -> error(identifiableProviders, 0, expectedType));
        }
    },
    DIRECT_MATCH {
        @Override
        @NotNull
        public <T> IdentifiableProvider<T> find(
                @NotNull final List<IdentifiableProvider<T>> identifiableProviders,
                @NotNull final TypeIdentifier<T> expectedType
        ) {
            final List<IdentifiableProvider<T>> directMatches = identifiableProviders.stream()
                    .filter(it -> it.type().equals(expectedType))
                    .toList();

            if (directMatches.size() != 1) {
                error(identifiableProviders, directMatches.size(), expectedType);
            }

            return directMatches.get(0);
        }
    },
    NONE {
        @Override
        @NotNull
        public <T> IdentifiableProvider<T> find(
                @NotNull final List<IdentifiableProvider<T>> identifiableProviders,
                @NotNull final TypeIdentifier<T> expectedType
        ) {
            return error(identifiableProviders, identifiableProviders.size(), expectedType);
        }
    },
    FIRST {
        @Override
        @NotNull
        public <T> IdentifiableProvider<T> find(
                @NotNull final List<IdentifiableProvider<T>> identifiableProviders,
                @NotNull final TypeIdentifier<T> expectedType
        ) {
            return identifiableProviders.stream()
                    .sorted()
                    .findFirst()
                    .orElseThrow(() -> buildError(identifiableProviders, 0, expectedType));
        }
    };

    public static final StandardWireConflictResolver DEFAULT = DIRECT_MATCH;

    public static StandardWireConflictResolver determine(String name) {
        String normalizedName = name.toUpperCase()
                .replaceAll("-", "_");
        return valueOf(normalizedName);
    }
}
