package com.wiredi.properties.converter;

import com.wiredi.properties.exceptions.InvalidPropertyTypeException;
import org.jetbrains.annotations.NotNull;

public class IntPropertyConverter implements PropertyConverter<Integer> {

	public static final IntPropertyConverter INSTANCE = new IntPropertyConverter();

	@Override
	public @NotNull Integer apply(@NotNull String key, @NotNull String value) {
		try {
			return Integer.parseInt(value);
		} catch (final NumberFormatException numberFormatException) {
			throw new InvalidPropertyTypeException(key, value, Integer.class);
		}
	}
}
