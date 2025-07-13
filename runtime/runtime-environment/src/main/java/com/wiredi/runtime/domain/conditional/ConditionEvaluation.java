package com.wiredi.runtime.domain.conditional;

import com.wiredi.runtime.Environment;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ConditionEvaluation implements Iterable<ConditionEvaluation.Context> {

    private final Map<IdentifiableProvider<?>, Context> evaluations = new HashMap<>();
    private final WireContainer wireContainer;

    public ConditionEvaluation(WireContainer wireContainer) {
        this.wireContainer = wireContainer;
    }

    public Context access(IdentifiableProvider<?> provider) {
        return evaluations.computeIfAbsent(provider, p -> new Context(wireContainer, p));
    }

    public Stream<Context> stream() {
        return evaluations.values().stream();
    }

    public List<Context> matched() {
        return evaluations.values().stream()
                .filter(Context::isMatched)
                .toList();
    }

    public List<Context> unmatched() {
        return evaluations.values().stream()
                .filter(it -> !it.isMatched())
                .toList();
    }

    @Override
    public @NotNull Iterator<Context> iterator() {
        return new ArrayList<>(evaluations.values()).iterator();
    }

    public static class Context {

        private final WireContainer wireContainer;
        private final IdentifiableProvider<?> provider;
        private AnnotationMetadata annotationMetadata;
        private final Set<String> dependencies = new HashSet<>();
        private final Set<String> positiveMatches = new HashSet<>();
        private final Set<String> negativeMatches = new HashSet<>();

        public Context(
                WireContainer wireContainer,
                IdentifiableProvider<?> provider
        ) {
            this.wireContainer = wireContainer;
            this.provider = provider;
        }

        @NotNull
        public AnnotationMetadata annotationMetadata() {
            return Objects.requireNonNull(annotationMetadata);
        }

        public void withAnnotationMetadata(AnnotationMetadata annotationMetadata, Consumer<Context> consumer) {
            AnnotationMetadata previous = this.annotationMetadata;
            this.annotationMetadata = annotationMetadata;
            try {
                consumer.accept(this);
            } finally {
                this.annotationMetadata = previous;
            }
        }

        public IdentifiableProvider<?> provider() {
            return provider;
        }

        public Environment environment() {
            return wireContainer.environment();
        }

        public WireContainer wireContainer() {
            return wireContainer;
        }

        public <T> T get(Class<T> type) {
            return wireContainer.onDemandInjector().get(type);
        }

        public void noteDependency(String dependency) {
            dependencies.add(dependency);
        }

        public void positiveMatch(String dependency) {
            positiveMatches.add(dependency);
        }

        public Set<String> dependencies() {
            return dependencies;
        }

        public Set<String> positiveMatches() {
            return positiveMatches;
        }

        public Set<String> negativeMatches() {
            return negativeMatches;
        }

        public void negativeMatch(String dependency) {
            negativeMatches.add(dependency);
        }

        public void reset() {
            dependencies.clear();
            positiveMatches.clear();
            negativeMatches.clear();
        }

        public boolean isMatched() {
            return negativeMatches.isEmpty();
        }
    }
}
