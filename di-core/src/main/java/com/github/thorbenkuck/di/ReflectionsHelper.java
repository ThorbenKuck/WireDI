package com.github.thorbenkuck.di;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;

public class ReflectionsHelper {

	public static void setField(String fieldName, Object object, Object value) {
		try {
			Field field = object.getClass().getDeclaredField(fieldName);
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

	public static void invokeMethod(Object object, String name, Object... parameters) {
		for(Object o : parameters) {
			Objects.requireNonNull(o, "Reflection based method invocation may only be used with non null instances");
		}
		Class[] classes = Stream.of(parameters)
				.map(Object::getClass)
				.toArray(Class[]::new);
		try {
			Method method = object.getClass().getDeclaredMethod(name, classes);

			boolean accessible = method.isAccessible();
			try {
				method.setAccessible(true);
				try {
					method.invoke(object, parameters);
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new IllegalStateException(e);
				}
			} finally {
				method.setAccessible(accessible);
			}
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

}
