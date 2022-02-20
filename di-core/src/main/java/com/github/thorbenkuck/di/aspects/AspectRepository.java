package com.github.thorbenkuck.di.aspects;

import com.github.thorbenkuck.di.DataAccess;
import com.github.thorbenkuck.di.domain.AspectFactory;
import com.github.thorbenkuck.di.domain.WireRepository;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

public class AspectRepository {

    private final Map<Class<? extends Annotation>, AspectWrapper<?>> aspectMappings = new HashMap<>();
    private final DataAccess dataAccess = new DataAccess();

    protected volatile boolean loaded;

    @SuppressWarnings("unchecked")
    public void load(WireRepository wireRepository) {
        if (loaded) {
            return;
        }
        dataAccess.write(() -> {
            if (loaded) {
                return;
            }

            ServiceLoader.load(AspectFactory.class)
                    .forEach(factory -> registerFor(factory.aroundAnnotation(), factory.build(wireRepository)));
            loaded = true;
        });
    }

    public void unload() {
        dataAccess.write(() -> {
            aspectMappings.clear();
            loaded = false;
        });
    }

    public <T extends Annotation> void registerFor(Class<T> annotationType, AspectInstance<T> aspectInstance) {
        dataAccess.write(() -> {
            if (aspectMappings.containsKey(annotationType)) {
                @SuppressWarnings("unchecked")
                AspectWrapper<T> currentHead = (AspectWrapper<T>) aspectMappings.get(annotationType);
                AspectWrapper<T> nextHead = currentHead.prepend(aspectInstance);
                aspectMappings.put(annotationType, nextHead);
            } else {
                aspectMappings.put(annotationType, new AspectWrapper<>(aspectInstance));
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> Optional<AspectWrapper<T>> access(Class<T> annotationType) {
        return dataAccess.read(() -> Optional.ofNullable((AspectWrapper<T>) aspectMappings.get(annotationType)));
    }

    public AspectExecutionContext startBuilder(Function<ExecutionContext, Object> realMethod) {
        return new AspectExecutionContext(this, realMethod);
    }
}
