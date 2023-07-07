package com.wiredi.properties.converter;

import org.jetbrains.annotations.NotNull;

public class BooleanPropertyConverter implements PropertyConverter<Boolean> {
	@Override
	public @NotNull Boolean apply(@NotNull String key,@NotNull String value) {
		return Boolean.parseBoolean(value);
	}
}
