package com.wiredi.runtime.properties.converter;

import com.wiredi.runtime.lang.Booleans;
import com.wiredi.runtime.properties.PropertyConverter;
import org.jetbrains.annotations.NotNull;

public class BooleanPropertyConverter implements PropertyConverter<Boolean> {

	public static final BooleanPropertyConverter INSTANCE = new BooleanPropertyConverter();

	@Override
	public @NotNull Boolean parse(
			@NotNull final Class<Boolean> type,
			@NotNull String propertyValue
	) {
		return Booleans.parseStrict(propertyValue);
	}

	@Override
	public @NotNull String stringify(@NotNull Boolean propertyValue) {
		return Boolean.toString(propertyValue);
	}
}
