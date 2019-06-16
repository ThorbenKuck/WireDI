package com.github.thorbenkuck.di;

import java.lang.reflect.Field;

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

}
