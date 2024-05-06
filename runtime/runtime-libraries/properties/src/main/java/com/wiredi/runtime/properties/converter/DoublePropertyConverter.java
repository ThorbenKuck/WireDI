package com.wiredi.runtime.properties.converter;

import com.wiredi.runtime.properties.PropertyConverter;
import com.wiredi.runtime.properties.exceptions.InvalidPropertyTypeException;
import org.jetbrains.annotations.NotNull;

public class DoublePropertyConverter implements PropertyConverter<Double> {

	public static final DoublePropertyConverter INSTANCE = new DoublePropertyConverter();

	@Override
	public @NotNull Double parse(
			@NotNull final Class<Double> type,
			@NotNull String propertyValue
	) {
		try {
			return Double.parseDouble(propertyValue);
		} catch (final NumberFormatException numberFormatException) {
			throw new InvalidPropertyTypeException(propertyValue, Integer.class);
		}
	}

	@Override
	public @NotNull String stringify(@NotNull Double propertyValue) {
		return Double.toString(propertyValue);
	}
}
