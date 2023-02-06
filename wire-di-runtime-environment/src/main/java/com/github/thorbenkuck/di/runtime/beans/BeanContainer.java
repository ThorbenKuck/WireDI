package com.github.thorbenkuck.di.runtime.beans;

import com.github.thorbenkuck.di.domain.WireCapable;
import com.github.thorbenkuck.di.domain.provider.TypeIdentifier;
import com.github.thorbenkuck.di.lang.DataAccess;
import com.github.thorbenkuck.di.runtime.Timed;
import com.github.thorbenkuck.di.runtime.exceptions.DiLoadingException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class BeanContainer<T extends WireCapable> {

    @NotNull
    private final Class<T> type;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @NotNull
    private final DataAccess dataAccess = new DataAccess();

    private volatile boolean loaded = false;

    @NotNull
    private final Map<TypeIdentifier<?>, List<T>> mapping = new HashMap<>();

    public BeanContainer(@NotNull Class<T> type) {
        this.type = type;
    }

    public void clear() {
        dataAccess.write(() -> {
            logger.info("Clearing cached mappings");
            mapping.clear();
            loaded = false;
        });
    }

    public Timed load() {
        // pre check to avoid unnecessary synchronization
        if (loaded) {
            return Timed.empty();
        }
        return dataAccess.write(() -> {
            // Check again, to combat race conditions,
            // where both threads pass the pre-checks
            // and then, one after another enter this
            // synchronized statement and then override
            // the same instances.
            // We want to ensure under any and all
            // circumstance, that we only load once.
            if (loaded) {
                return Timed.empty();
            }

            logger.debug("Starting to load {}", type);
            Timed timed = Timed.of(() -> {
                ServiceLoader.load(type)
                        .forEach(this::register);
                ServiceLoader.loadInstalled(type)
                        .forEach(this::register);

                loaded = true;
            });

            logger.info("Loading finished in {}ms", timed.get(TimeUnit.MILLISECONDS));
            return timed;
        });
    }

    public final void register(@NotNull final T t) {
        dataAccess.write(() -> {
            logger.debug("Registering instance of type {} with wired types {}", t.getClass(), Arrays.toString(t.wiredTypes()));
            for (final TypeIdentifier<?> wiredType : t.wiredTypes()) {
                if (wiredType == null) {
                    throw new DiLoadingException("The WireCables " + t + " returned null as an identifiable type! This is not permitted.\n" +
                            "If you did not create your own instance, please submit your annotated class to github.");
                }
                logger.trace("Registering {} for {}", t, wiredType);
                unsafeRegister(wiredType, t);
            }
        });
    }

    public final boolean isLoaded() {
        return loaded;
    }

    public void unsafeRegister(@NotNull final TypeIdentifier<?> type, @NotNull final T instance) {
        final List<T> providers = mapping.computeIfAbsent(type, (t) -> new ArrayList<>());
        providers.add(instance);
    }

    @NotNull
    public Map<TypeIdentifier<?>, List<T>> getAll() {
        return mapping;
    }

    @NotNull
    public List<T> getAll(@NotNull final Class<?> type) {
        return getAll(TypeIdentifier.of(type));
    }

    @NotNull
    public Stream<T> stream(@NotNull final Class<?> type) {
        return stream(TypeIdentifier.of(type));
    }

    @NotNull
    public List<T> getAll(@NotNull final TypeIdentifier<?> type) {
        return dataAccess.read(() -> new ArrayList<>(mapping.getOrDefault(type, Collections.emptyList())));
    }

    @NotNull
    public Stream<T> stream(@NotNull final TypeIdentifier<?> type) {
        return getAll(type).stream();
    }

    @Override
    @NotNull
    public final String toString() {
        return getClass().getSimpleName() + "{" +
                "registrations=" + getAll() +
                ", loaded=" + loaded +
                '}';
    }
}
