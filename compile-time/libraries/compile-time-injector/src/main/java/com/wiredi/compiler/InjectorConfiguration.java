package com.wiredi.compiler;

import com.wiredi.runtime.lang.ReflectionsHelper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class InjectorConfiguration {

    private final List<Class<? extends Annotation>> injectionQualifiers = new ArrayList<>();
    private final List<Class<? extends Annotation>> postConstructQualifiers = new ArrayList<>();
    private final List<Class<? extends Annotation>> preDestroyQualifiers = new ArrayList<>();
    private final Map<Class<?>, Set<Method>> postConstructMethods = new HashMap<>();
    private final Map<Class<?>, Set<Method>> preDestroyMethods = new HashMap<>();
    private final Map<Class<?>, Set<Method>> injectionMethods = new HashMap<>();
    private final Map<Class<?>, Set<Field>> injectionFields = new HashMap<>();
    private boolean singletonFirst = true;

    public InjectorConfiguration() {
        injectionQualifiers.add(Inject.class);
        postConstructQualifiers.add(PostConstruct.class);
        preDestroyQualifiers.add(PreDestroy.class);
    }

    /**
     * Whether created instances should be treated as singleton by default or not.
     *
     * @param singletonFirst if true, all instances will be singleton by default
     * @return this
     */
    public InjectorConfiguration singletonFirst(boolean singletonFirst) {
        this.singletonFirst = singletonFirst;
        return this;
    }

    public InjectorConfiguration addInjectionQualifier(Class<? extends Annotation> injectionQualifier) {
        injectionQualifiers.add(injectionQualifier);
        return this;
    }

    public InjectorConfiguration addPostConstructQualifier(Class<? extends Annotation> postConstructQualifier) {
        postConstructQualifiers.add(postConstructQualifier);
        return this;
    }

    public Set<Field> findAllInjectionFields(Object instance) {
        return injectionFields.computeIfAbsent(instance.getClass(), t -> {
            Set<Field> fields = new HashSet<>();
            injectionQualifiers.forEach(qualifier -> {
                fields.addAll(ReflectionsHelper.findAllAnnotatedFields(instance, qualifier));
            });

            return fields;
        });
    }

    public Set<Method> findAllInjectionMethods(Object instance) {
        return injectionMethods.computeIfAbsent(instance.getClass(), t -> {
            Set<Method> fields = new HashSet<>();
            injectionQualifiers.forEach(qualifier -> {
                fields.addAll(ReflectionsHelper.findAllAnnotatedMethods(instance, qualifier));
            });

            return fields;
        });
    }

    public Set<Method> findAllPostConstructMethods(Object instance) {
        return postConstructMethods.computeIfAbsent(instance.getClass(), t -> {
            Set<Method> fields = new HashSet<>();
            postConstructQualifiers.forEach(qualifier -> {
                fields.addAll(ReflectionsHelper.findAllAnnotatedMethods(instance, qualifier));
            });

            return fields;
        });
    }

    public Set<Method> findAllPreDestroyMethods(Object instance) {
        return preDestroyMethods.computeIfAbsent(instance.getClass(), t -> {
            Set<Method> fields = new HashSet<>();
            preDestroyQualifiers.forEach(qualifier -> {
                fields.addAll(ReflectionsHelper.findAllAnnotatedMethods(instance, qualifier));
            });

            return fields;
        });
    }

    public boolean isSingleton(Class<?> type) {
        return singletonFirst || type.isAnnotationPresent(Singleton.class);
    }

    public boolean isInjectionPoint(AnnotatedElement constructor) {
        return injectionQualifiers.stream().anyMatch(constructor::isAnnotationPresent);
    }
}
