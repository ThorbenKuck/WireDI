package com.wiredi.properties.converter;

import com.wiredi.properties.exceptions.InvalidPropertyTypeException;
import org.jetbrains.annotations.NotNull;

public class DoublePropertyConverter implements PropertyConverter<Double> {

	public static final DoublePropertyConverter INSTANCE = new DoublePropertyConverter();

	@Override
	public @NotNull Double apply(@NotNull String key, @NotNull String value) {
		try {
			return Double.parseDouble(value);
		} catch (final NumberFormatException numberFormatException) {
			throw new InvalidPropertyTypeException(key, value, Integer.class);
		}
	}
}
