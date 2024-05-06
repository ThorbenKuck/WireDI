package com.wiredi.runtime.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wiredi.runtime.lang.Preconditions.isNotNull;

public final class ReflectionsHelper {

    public static void setField(
            @NotNull final String fieldName,
            @NotNull final Object instance,
            @NotNull final Class<?> type,
            @Nullable final Object value
    ) {
        final Field field;
        try {
            field = type.getDeclaredField(fieldName);
        } catch (final NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
        setField(instance, field, value);
    }

    public static void setField(
            @NotNull final Object instance,
            @NotNull final Field field,
            @Nullable final Object value
    ) {
        if (!field.trySetAccessible()) {
            throw new IllegalStateException("Could not make the field " + field + " accessible.");
        }
        try {
            field.set(instance, value);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void invokeMethod(
            @NotNull final Object instance,
            @NotNull final Class<?> instanceType,
            @NotNull final String name,
            @NotNull final Class<?> returnValue,
            final Object... rawParameters
    ) {
        for (final Object o : rawParameters) {
            Objects.requireNonNull(o, "Reflection based method invocation may only be used with non null instances");
        }
        final Class<?>[] parameters = Stream.of(rawParameters)
                .map(Object::getClass)
                .toArray(Class[]::new);

        final Method method = findMethod(instanceType, name, parameters, returnValue);

        if (method == null) {
            throw new IllegalStateException("Could not find the method " + returnValue.getSimpleName() + " " + name + "(" + Arrays.toString(parameters) + ") on " + instance);
        }

        invokeMethod(instance, method, rawParameters);
    }

    public static void invokeMethod(
            @NotNull final Object instance,
            @NotNull final Method method,
            @NotNull final Object[] parameters
    ) {
        if (parameters.length != method.getParameterCount()) {
            throw new IllegalArgumentException("Cannot invoke the method " + method + " using the parameters " + Arrays.toString(parameters));
        }
        if (!method.trySetAccessible()) {
            throw new IllegalStateException("Could not make the method " + method + " accessible.");
        }

        try {
            method.invoke(instance, parameters);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (final Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    @Nullable
    public static <A extends Annotation> A findAnnotationOnMethod(
            @NotNull Class<?> type,
            @NotNull Class<A> annotationType,
            @NotNull String name,
            @NotNull Class<?>[] parameterNames,
            @NotNull Class<?> returnValue
    ) {
        final Method declaredMethod = findMethod(type, name, parameterNames, returnValue);
        if (declaredMethod == null) {
            return null;
        } else {
            return declaredMethod.getAnnotation(annotationType);
        }
    }

    @NotNull
    public static <A extends Annotation> A requireAnnotationOnMethod(
            @NotNull Class<?> type,
            @NotNull Class<A> annotationType,
            @NotNull String name,
            @NotNull Class<?>[] parameterNames,
            @NotNull Class<?> returnValue
    ) {
        return isNotNull(
                findAnnotationOnMethod(type, annotationType, name, parameterNames, returnValue),
                () -> "Could not find the annotation @" + annotationType.getSimpleName()
                        + " on " + type.getName() + "." + name + "(" + Arrays.stream(parameterNames)
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(", "))
                        + ")"
        );
    }

    @Nullable
    public static Method findMethod(
            @NotNull final Class<?> type,
            @NotNull final String name,
            @NotNull final Class<?>[] parameters,
            @NotNull final Class<?> returnType
    ) {
        final List<Method> collect = Arrays.stream(type.getDeclaredMethods())
                .filter(it -> it.getName().equals(name)
                        && Arrays.equals(it.getParameterTypes(), parameters)
                        && it.getReturnType().equals(returnType)
                ).toList();

        if (collect.isEmpty()) {
            return null;
        } else if (collect.size() > 1) {
            throw new IllegalStateException("Expected to find the method " + type + " " + returnType + " " + name + Arrays.toString(parameters));
        } else {
            return collect.get(0);
        }
    }

    public static List<Field> getAnnotatedFields(Class<?> type, Class<? extends Annotation> annotation) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(annotation))
                .toList();
    }

    public static List<Method> getAnnotatedMethods(Class<?> type, Class<? extends Annotation> annotation) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(annotation))
                .toList();
    }

    public static List<Field> findAllAnnotatedFields(Object instance, Class<? extends Annotation> annotation) {
        List<Field> returnValue = new ArrayList<>();
        Class<?> rootType = instance.getClass();

        while (rootType != Object.class) {
            returnValue.addAll(getAnnotatedFields(rootType, annotation));
            rootType = rootType.getSuperclass();
        }

        return returnValue;
    }

    public static List<Method> findAllAnnotatedMethods(Object instance, Class<? extends Annotation> annotation) {
        List<Method> returnValue = new ArrayList<>();
        Class<?> rootType = instance.getClass();

        while (rootType != Object.class) {
            returnValue.addAll(getAnnotatedMethods(rootType, annotation));
            rootType = rootType.getSuperclass();
        }

        return returnValue;
    }

    @NotNull
    public static List<Class<?>> getAllInheritedClasses(Class<?> rootClass) {
        List<Class<?>> inheritedClasses = new ArrayList<>();
        if (rootClass.getSuperclass() != null) {
            inheritedClasses.addAll(getAllInheritedClasses(rootClass.getSuperclass()));
            inheritedClasses.add(rootClass.getSuperclass());
        }
        for (Class<?> anInterface : rootClass.getInterfaces()) {
            inheritedClasses.addAll(getAllInheritedClasses(anInterface));
            inheritedClasses.add(anInterface);
        }
        return inheritedClasses;
    }
}
