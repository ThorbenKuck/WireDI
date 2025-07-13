package com.wiredi.runtime;

import com.wiredi.runtime.collections.TypeMap;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.lang.ReflectionsHelper;
import com.wiredi.runtime.lang.SingletonSupplier;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class OnDemandInjector {

    private static final Map<WireContainer, OnDemandInjector> INSTANCES = new HashMap<>();
    private final WireContainer wireContainer;
    private final TypeMap<Object> cache = new TypeMap<>();
    private final TypeMap<Class<?>> typeTranslations = new TypeMap<>();
    private final TypeMap<Supplier<?>> constructors = new TypeMap<>();

    OnDemandInjector(WireContainer wireContainer) {
        this.wireContainer = wireContainer;
        bind(OnDemandInjector.class).toValue(this);
        bind(WireContainer.class).toValue(wireContainer);
    }

    public static OnDemandInjector of(WireContainer wireContainer) {
        return INSTANCES.computeIfAbsent(wireContainer, OnDemandInjector::new);
    }

    public void clearCache() {
        cache.clear();
    }

    public void clear() {
        clearCache();
        typeTranslations.clear();
        constructors.clear();
    }

    public <T> T get(Class<T> type) {
        if (wireContainer.contains(type)) {
            return wireContainer.get(type);
        }

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
        if (IdentifiableProvider.class.equals(type)) {
            return (T) wireContainer.getNativeProvider(TypeIdentifier.just(type));
        }
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
            Object fieldInstance = getFromWireRepositoryOrCreate(field.getGenericType());
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

    private <T> T getFromWireRepositoryOrCreate(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType().equals(IdentifiableProvider.class)) {
                Class<?> genericType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                return (T) wireContainer.getNativeProvider(TypeIdentifier.just(genericType));
            }

            Class<T> genericType = (Class<T>) type;
            return wireContainer.tryGet(genericType).orElseGet(() -> this.get(genericType));
        }

        if (type instanceof Class<?>) {
            Class<T> genericType = (Class<T>) type;
            return (T) wireContainer.tryGet(genericType).orElseGet(() -> this.get(genericType));
        }
        throw new IllegalStateException("Unsupported type " + type);
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
