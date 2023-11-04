package com.wiredi.runtime;

import com.wiredi.domain.WireRepositoryContextCallbacks;
import com.wiredi.lang.ReflectionsHelper;
import com.wiredi.lang.SingletonSupplier;
import com.wiredi.lang.collections.TypeMap;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class OnDemandInjector {

    private final WireRepository wireRepository;
    private final TypeMap<Object> cache = new TypeMap<>();
    private final TypeMap<Class<?>> typeTranslations = new TypeMap<>();
    private final TypeMap<Supplier<?>> constructors = new TypeMap<>();
    private static final Map<WireRepository, OnDemandInjector> INSTANCES = new HashMap<>();

    OnDemandInjector(WireRepository wireRepository) {
        this.wireRepository = wireRepository;
        wireRepository.register(OnDemandInjectorUnRegistrationCallback.INSTANCE);
        bind(OnDemandInjector.class).toValue(this);
    }

    public static class OnDemandInjectorUnRegistrationCallback implements WireRepositoryContextCallbacks {

        private static final WireRepositoryContextCallbacks INSTANCE = new OnDemandInjectorUnRegistrationCallback();

        @Override
        public void destroyed(@NotNull WireRepository wireRepository) {
            INSTANCES.remove(wireRepository);
        }
    }

    public static OnDemandInjector of(WireRepository wireRepository) {
        return INSTANCES.computeIfAbsent(wireRepository, OnDemandInjector::new);
    }

    public void clearCache() {
        cache.clear();
    }

    public void clear() {
        clearCache();
        typeTranslations.clear();
    }

    public <T> T get(Class<T> type) {
        if (cache.containsKey(type)) {
            return (T) cache.get(type);
        }
        return createOnDemandInstanceOf(type);
    }

    private <T> T createOnDemandInstanceOf(Class<T> type) {
        var translatedType = (Class<T>) typeTranslations.getOrDefault(type, type);
        T instance = createNewInstance(translatedType);
        injectInto(instance);
        postConstruction(instance);
        cache.put(type, instance);
        return instance;
    }

    private <T> T createNewInstance(Class<T> type) {
        return (T) findTargetConstructor(type)
                .map(this::invokeConstructor)
                .orElseGet(() -> newInstanceFromDefaultConstructor(type));
    }

    private <T> T newInstanceFromDefaultConstructor(Class<T> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private Optional<Constructor<?>> findTargetConstructor(Class<?> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();

        if (constructors.length == 0) {
            return Optional.empty();
        }

        Constructor<?> constructor;
        if (constructors.length == 1) {
            constructor = constructors[0];
        } else {
            var annotatedConstructors = Arrays.stream(constructors)
                    .filter(it -> it.isAnnotationPresent(Inject.class))
                    .toList();

            if (annotatedConstructors.size() != 1) {
                throw new IllegalStateException("Please provide a single constructor with @Inject on " + type);
            }
            constructor = annotatedConstructors.get(0);
        }

        return Optional.of(constructor);
    }

    private <T> T invokeConstructor(Constructor<?> constructor) {
        var args = Arrays.stream(constructor.getParameterTypes())
                .map(this::getFromWireRepositoryOrCreate)
                .toList()
                .toArray();

        try {
            constructor.trySetAccessible();
            return (T) constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public <T> T injectInto(T instance) {
        var fields = ReflectionsHelper.findAllAnnotatedFields(instance, Inject.class);

        for (Field field : fields) {
            Object fieldInstance = getFromWireRepositoryOrCreate(field.getType());
            field.trySetAccessible();
            try {
                field.set(instance, fieldInstance);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        return instance;
    }

    public void postConstruction(Object instance) {
        var methods = ReflectionsHelper.findAllAnnotatedMethods(instance, PostConstruct.class);

        for (Method method : methods) {
            if (method.getParameterTypes().length == 0) {
                method.trySetAccessible();
                try {
                    method.invoke(instance);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    public <T> BindStage<T> bind(Class<T> type) {
        return new BindStage<>(type, this);
    }

    private <T> T getFromWireRepositoryOrCreate(Class<T> type) {
        return wireRepository.tryGet(type).orElseGet(() -> this.get(type));
    }

    public record BindStage<T>(Class<T> type, OnDemandInjector injector) {

        public <S extends T> void to(Class<S> other) {
            injector.typeTranslations.put(type, other);
        }

        public <S extends T> S toValue(S instance) {
            injector.cache.put(type, instance);
            return instance;
        }

        public <S extends T> OnDemandInjector to(S instance) {
            return toSupplier(new SingletonSupplier<>(instance));
        }

        public <S extends T> OnDemandInjector toSupplier(Supplier<S> supplier) {
            injector.constructors.put(type, supplier);
            return injector;
        }
    }
}
