package com.github.thorbenkuck.di.aspects;

import com.github.thorbenkuck.di.DataAccess;
import com.github.thorbenkuck.di.annotations.ManualWireCandidate;
import com.github.thorbenkuck.di.domain.AspectFactory;
import com.github.thorbenkuck.di.domain.WireRepository;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

@ManualWireCandidate
public class AspectRepository {

    @NotNull
    private final Map<Class<? extends Annotation>, AspectWrapper<?>> aspectMappings = new HashMap<>();
    @NotNull
    private final DataAccess dataAccess = new DataAccess();

    protected volatile boolean loaded = false;

    @SuppressWarnings("unchecked")
    public void load(@NotNull WireRepository wireRepository) {
        if (loaded) {
            return;
        }
        dataAccess.write(() -> {
            if (loaded) {
                return;
            }

            ServiceLoader.load(AspectFactory.class)
                    .forEach(factory -> {
                        final AspectInstance<?> build = factory.build(wireRepository);
                        registerFor(factory.aroundAnnotation(), build);
                    });
            loaded = true;
        });
    }

    public void unload() {
        dataAccess.write(() -> {
            aspectMappings.clear();
            loaded = false;
        });
    }

    public <T extends Annotation> void registerFor(
            @NotNull final Class<T> annotationType,
            @NotNull final AspectInstance<T> aspectInstance
    ) {
        dataAccess.write(() -> {
            if (aspectMappings.containsKey(annotationType)) {
                @SuppressWarnings("unchecked")
                final AspectWrapper<T> currentHead = (AspectWrapper<T>) aspectMappings.get(annotationType);
                final AspectWrapper<T> nextHead = currentHead.prepend(aspectInstance);
                aspectMappings.put(annotationType, nextHead);
            } else {
                aspectMappings.put(annotationType, new AspectWrapper<>(aspectInstance));
            }
        });
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public <T extends Annotation> Optional<AspectWrapper<T>> access(@NotNull final Class<T> annotationType) {
        return dataAccess.read(() -> Optional.ofNullable((AspectWrapper<T>) aspectMappings.get(annotationType)));
    }

    @NotNull
    public AspectExecutionContext startBuilder(@NotNull final Function<ExecutionContext<?>, Object> realMethod) {
        return new AspectExecutionContext(this, realMethod);
    }
}
