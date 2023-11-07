package com.wiredi.properties.converter;

import com.wiredi.properties.exceptions.InvalidPropertyTypeException;
import org.jetbrains.annotations.NotNull;

public class FloatPropertyConverter implements PropertyConverter<Float> {

	public static final FloatPropertyConverter INSTANCE = new FloatPropertyConverter();

	@Override
	public @NotNull Float apply(@NotNull String key, @NotNull String value) {
		try {
			return Float.parseFloat(value);
		} catch (final NumberFormatException numberFormatException) {
			throw new InvalidPropertyTypeException(key, value, Integer.class);
		}
	}
}
