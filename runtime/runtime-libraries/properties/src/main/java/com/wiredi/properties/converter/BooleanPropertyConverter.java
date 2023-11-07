package com.wiredi.properties.converter;

import com.wiredi.lang.Booleans;
import org.jetbrains.annotations.NotNull;

public class BooleanPropertyConverter implements PropertyConverter<Boolean> {

	public static final BooleanPropertyConverter INSTANCE = new BooleanPropertyConverter();

	@Override
	public @NotNull Boolean apply(@NotNull String key,@NotNull String value) {
		return Booleans.parseStrict(value);
	}
}
