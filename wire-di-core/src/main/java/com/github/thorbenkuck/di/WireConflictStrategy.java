package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.domain.IdentifiableProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public enum WireConflictStrategy implements WireConflictResolver {
    BEST_MATCH {
        @Override
        @NotNull
        public <T> IdentifiableProvider<T> find(
                @NotNull final List<IdentifiableProvider<T>> identifiableProviders,
                @NotNull final Class<T> expectedType
        ) {
            final List<IdentifiableProvider<T>> bestMatches = identifiableProviders.stream()
                    .sorted((o1, o2) -> {
                        final boolean firstDirectlyMatches = expectedType.equals(o1.type());
                        final boolean secondDirectlyMatches = expectedType.equals(o2.type());
                        final int compare = Boolean.compare(firstDirectlyMatches, secondDirectlyMatches);

                        if (compare == 0) {
                            return o1.compareTo(o2);
                        } else {
                            return compare;
                        }
                    })
                    .collect(Collectors.toList());

            if (bestMatches.isEmpty()) {
                error(identifiableProviders, 0, expectedType);
            }

            return bestMatches.get(0);
        }
    },
    FIRST_DIRECT_MATCH {
        @Override
        @NotNull
        public <T> IdentifiableProvider<T> find(
                @NotNull final List<IdentifiableProvider<T>> identifiableProviders,
                @NotNull final Class<T> expectedType
        ) {
            return identifiableProviders.stream()
                    .filter(it -> it.type().equals(expectedType))
                    .findFirst()
                    .orElseGet(() -> error(identifiableProviders, 0, expectedType));
        }
    },
    DIRECT_MATCH {
        @Override
        @NotNull
        public <T> IdentifiableProvider<T> find(
                @NotNull final List<IdentifiableProvider<T>> identifiableProviders,
                @NotNull final Class<T> expectedType
        ) {
            final List<IdentifiableProvider<T>> directMatches = identifiableProviders.stream()
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
        @NotNull
        public <T> IdentifiableProvider<T> find(
                @NotNull final List<IdentifiableProvider<T>> identifiableProviders,
                @NotNull final Class<T> expectedType
        ) {
            return error(identifiableProviders, identifiableProviders.size(), expectedType);
        }
    },
    FIRST {
        @Override
        @NotNull
        public <T> IdentifiableProvider<T> find(
                @NotNull final List<IdentifiableProvider<T>> identifiableProviders,
                @NotNull final Class<T> expectedType
        ) {
            return identifiableProviders.stream()
                    .findFirst()
                    .orElseThrow(() -> buildError(identifiableProviders, 0, expectedType));
        }
    };

    public static final WireConflictStrategy DEFAULT = DIRECT_MATCH;

    @NotNull
    public <T> IdentifiableProvider<T> error(
            @NotNull final List<IdentifiableProvider<T>> total,
            final int match,
            @NotNull final Class<T> type
    ) throws DiInstantiationException {
        throw buildError(total, match, type);
    }

    @NotNull
    public <T> DiInstantiationException buildError(
            @NotNull final List<IdentifiableProvider<T>> total,
            final int match,
            @NotNull final Class<T> type
    ) {
        final StringBuilder result = new StringBuilder();
        result.append("Expected to find exactly 1 Provider for type ")
                .append(type)
                .append(" using the ")
                .append(name())
                .append(" strategy, but got ")
                .append(match)
                .append(" out of ")
                .append(total.size())
                .append(" potential candidates")
                .append(System.lineSeparator())
                .append("Candidates: ")
                .append(System.lineSeparator());

        total.forEach(provider -> result.append(" - ").append(provider.toString()).append(System.lineSeparator()));
        return new DiInstantiationException(result.toString());
    }
}
