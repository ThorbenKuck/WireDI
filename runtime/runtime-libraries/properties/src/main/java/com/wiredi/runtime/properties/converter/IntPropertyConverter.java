package com.wiredi.runtime.properties.converter;

import com.wiredi.runtime.properties.PropertyConverter;
import com.wiredi.runtime.properties.exceptions.InvalidPropertyTypeException;
import org.jetbrains.annotations.NotNull;

public class IntPropertyConverter implements PropertyConverter<Integer> {

	public static final IntPropertyConverter INSTANCE = new IntPropertyConverter();

	@Override
	public @NotNull Integer parse(
			@NotNull final Class<Integer> type,
			@NotNull String propertyValue
	) {
		try {
			return Integer.parseInt(propertyValue);
		} catch (final NumberFormatException numberFormatException) {
			throw new InvalidPropertyTypeException(propertyValue, Integer.class);
		}
	}

	@Override
	public @NotNull String stringify(@NotNull Integer propertyValue) {
		return Integer.toString(propertyValue);
	}
}
