package com.github.thorbenkuck.di;

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

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    public static void setField(String fieldName, Object object, Class<?> type, Object value) {
        try {
            Field field = type.getDeclaredField(fieldName);
            boolean access = field.isAccessible();
            try {
                field.setAccessible(true);
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            } finally {
                field.setAccessible(access);
            }
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void invokeMethod(Object instance, Class<?> type, String name, Class<?> returnValue, Object... rawParameters) {
        for (Object o : rawParameters) {
            Objects.requireNonNull(o, "Reflection based method invocation may only be used with non null instances");
        }
        Class[] parameters = Stream.of(rawParameters)
                .map(Object::getClass)
                .toArray(Class[]::new);

        Method method = findMethod(type, name, parameters, returnValue);

        if (method == null) {
            throw new IllegalStateException("Could not find the method " + returnValue.getSimpleName() + " " + name + "(" + Arrays.toString(parameters) + ") on " + instance);
        }

		boolean accessible = method.isAccessible();
        try {
			method.setAccessible(true);
			method.invoke(instance, rawParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (Throwable e) {
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
            @NotNull Class[] parameterNames,
            @NotNull Class<?> returnValue
    ) {
        Method declaredMethod = findMethod(type, name, parameterNames, returnValue);
		if(declaredMethod == null) {
			return null;
		} else {
			return declaredMethod.getAnnotation(annotationType);
		}
    }

    public static Method findMethod(Class<?> type, String name, Class[] parameters, Class<?> returnType) {
        List<Method> collect = Arrays.stream(type.getDeclaredMethods())
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
