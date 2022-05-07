package com.github.thorbenkuck.di.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReflectionsHelper {

    public static void setField(
            @NotNull final String fieldName,
            @NotNull final Object object,
            @NotNull final Class<?> type,
            @Nullable final Object value
    ) {
        try {
            final Field field = type.getDeclaredField(fieldName);
            final boolean access = field.isAccessible();
            try {
                field.setAccessible(true);
                field.set(object, value);
            } catch (final IllegalAccessException e) {
                throw new IllegalStateException(e);
            } finally {
                field.setAccessible(access);
            }
        } catch (final NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void invokeMethod(
            @NotNull final Object instance,
            @NotNull final Class<?> type,
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

        final Method method = findMethod(type, name, parameters, returnValue);

        if (method == null) {
            throw new IllegalStateException("Could not find the method " + returnValue.getSimpleName() + " " + name + "(" + Arrays.toString(parameters) + ") on " + instance);
        }

		final boolean accessible = method.isAccessible();
        try {
			method.setAccessible(true);
			method.invoke(instance, rawParameters);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (final Throwable e) {
            throw new UndeclaredThrowableException(e);
        } finally {
			method.setAccessible(accessible);
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
		if(declaredMethod == null) {
			return null;
		} else {
			return declaredMethod.getAnnotation(annotationType);
		}
    }

    @Nullable
    public static Method findMethod(
            @NotNull final Class<?> type,
            @NotNull final String name,
            @NotNull final Class<?>[] parameters,
            @NotNull final Class<?> returnType
    ) {
        final List<Method> collect = Arrays.stream(type.getDeclaredMethods())
                .filter(it -> it.getName().equals(name))
                .filter(it -> Arrays.equals(it.getParameterTypes(), parameters))
                .filter(it -> it.getReturnType().equals(returnType))
                .collect(Collectors.toList());

        if (collect.isEmpty()) {
            return null;
        } else if (collect.size() > 1) {
			throw new IllegalStateException("This... well... Should not be possible...");
        } else {
            return collect.get(0);
        }
    }
}
