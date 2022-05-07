package com.github.thorbenkuck.di.aspects;

import com.github.thorbenkuck.di.lang.DataAccess;
import com.github.thorbenkuck.di.runtime.Timed;
import com.github.thorbenkuck.di.runtime.WireRepository;
import com.github.thorbenkuck.di.annotations.ManualWireCandidate;
import com.github.thorbenkuck.di.domain.aop.AspectFactory;
import com.github.thorbenkuck.di.runtime.WiredTypes;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@ManualWireCandidate
public class AspectRepository {

    @NotNull
    private final Map<Class<? extends Annotation>, AspectWrapper<?>> aspectMappings = new HashMap<>();
    @NotNull
    private final DataAccess dataAccess = new DataAccess();

    protected volatile boolean loaded = false;

    private static final Logger logger = LoggerFactory.getLogger(WiredTypes.class);

    public void load(@NotNull WireRepository wireRepository) {
        if (loaded) {
            return;
        }
        dataAccess.write(() -> {
            if (loaded) {
                return;
            }
            Timed timed = Timed.of(() -> {
                logger.debug("Starting to load all AspectFactories");
                ServiceLoader.load(AspectFactory.class)
                        .forEach(factory -> {
                            final AspectInstance<?> build = factory.build(wireRepository);
                            registerFor(factory.aroundAnnotation(), build);
                        });

                loaded = true;
            });
            logger.info("Loading finished in {}ms", timed.get(TimeUnit.MILLISECONDS));
        });
    }

    public void unload() {
        dataAccess.write(() -> {
            logger.info("Clearing cached mappings");
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
                final AspectWrapper<T> currentHead = (AspectWrapper<T>) aspectMappings.get(annotationType);
                final AspectWrapper<T> nextHead = currentHead.prepend(aspectInstance);
                aspectMappings.put(annotationType, nextHead);
            } else {
                aspectMappings.put(annotationType, new AspectWrapper<>(aspectInstance));
            }
        });
    }

    @NotNull
    public <T extends Annotation> Optional<AspectWrapper<T>> access(@NotNull final Class<T> annotationType) {
        return dataAccess.read(() -> Optional.ofNullable((AspectWrapper<T>) aspectMappings.get(annotationType)));
    }

    @NotNull
    public AspectExecutionContext startBuilder(@NotNull final Function<ExecutionContext<?>, Object> realMethod) {
        return new AspectExecutionContext(this, realMethod);
    }
}
