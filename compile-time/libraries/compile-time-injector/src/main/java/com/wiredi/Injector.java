package com.wiredi;

import com.wiredi.lang.ReflectionsHelper;
import com.wiredi.lang.SingletonSupplier;
import com.wiredi.lang.TypeMap;
import jakarta.inject.Inject;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

public class Injector {

    public Injector() {
        bind(Injector.class).toValue(this);
    }

    private final TypeMap<Object, Object> cache = new TypeMap<>();
    private final TypeMap<Object, TypeConstructor<?, ?>> constructors = new TypeMap<>();
    private final TypeMap<Object, Class<?>> typeTranslations = new TypeMap<>();

    public <T> T get(Class<T> type) {
        if (constructors.containsKey(type)) {
            ConstructionResult<T> supplierValue = ((TypeConstructor<T, T>) constructors.get(type)).construct(getCaller(), type);
            T value = injectInto(supplierValue);
            if (supplierValue.cache()) {
                cache.put(type, value);
            } else {
                return value;
            }
        }
        if(cache.containsKey(type)) {
            return (T) cache.get(type);
        }
        T instance = create(type);
        injectInto(instance);
        cache.put(type, instance);
        return instance;
    }

    public <T> T injectInto(ConstructionResult<T> result) {
        if (result.value() == null) {
            return null;
        }

        return injectInto(result.value());
    }

    public <T> T injectInto(T instance) {
        var fields = ReflectionsHelper.findAllAnnotatedFields(instance, Inject.class);

        for (Field field : fields) {
            Object fieldInstance = get(field.getType());
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

    public void clear() {
        cache.clear();
        typeTranslations.clear();
        constructors.clear();
    }

    public <T> BindStage<T> bind(Class<T> type) {
        return new BindStage<>(type, this);
    }

    public record BindStage<T>(Class<T> type, Injector injector) {

        public <S extends T> void to(Class<S> other) {
            injector.typeTranslations.put(type, other);
        }

        public <S extends T> S toValue(S instance) {
            injector.cache.put(type, instance);
            return instance;
        }

        public <S extends T> Injector to(S instance) {
            return toSupplier(new SingletonSupplier<>(instance));
        }

        public <S extends T> Injector toConstructor(TypeConstructor<T, S> instance) {
            injector.constructors.put(type, instance);
            return injector;
        }

        public <S extends T> Injector toSupplier(Supplier<S> supplier) {
            return toConstructor(TypeConstructor.wrap(supplier));
        }
    }

    private <T> T create(Class<T> type) {
        var translatedType = (Class<T>) typeTranslations.getOrDefault(type, type);
        T instance = createInstance(translatedType);
        injectInto(instance);
        postConstruction(instance);
        return instance;
    }

    private <T> T createInstance(Class<T> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();

        if(constructors.length == 0) {
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }

        Constructor<?> constructor;
        if(constructors.length == 1) {
            constructor = constructors[0];
        } else {
            var annotatedConstructors = Arrays.stream(constructors)
                    .filter(it -> it.isAnnotationPresent(Inject.class))
                    .toList();

            if(annotatedConstructors.size() != 1) {
                throw new IllegalStateException("Please provide a single constructor with @Inject on " + type);
            }
            constructor = annotatedConstructors.get(0);
        }

        var args = Arrays.stream(constructor.getParameterTypes())
                .map(this::get)
                .toList()
                .toArray();
        try {
            constructor.trySetAccessible();
            return (T) constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private Class<?> getCaller() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(Injector.class.getName()) && ste.getClassName().indexOf("java.lang.Thread")!=0) {
                try {
                    return Class.forName(ste.getClassName());
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return null;

    }
}
