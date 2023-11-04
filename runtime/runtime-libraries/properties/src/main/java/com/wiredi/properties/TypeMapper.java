package com.wiredi.properties;

import com.wiredi.lang.collections.TypeMap;
import com.wiredi.properties.converter.*;
import com.wiredi.properties.exceptions.MissingTypeConverterException;
import com.wiredi.properties.keys.Key;
import org.jetbrains.annotations.NotNull;

public class TypeMapper {

	@NotNull
	private static final TypeMap<PropertyConverter<?>> typeMappings = new TypeMap<>();

	static {
		setTypeConverter(boolean.class, new BooleanPropertyConverter());
		setTypeConverter(int.class, new IntPropertyConverter());
		setTypeConverter(float.class, new FloatPropertyConverter());
		setTypeConverter(double.class, new DoublePropertyConverter());

		setTypeConverter(Boolean.class, new BooleanPropertyConverter());
		setTypeConverter(Integer.class, new IntPropertyConverter());
		setTypeConverter(Float.class, new FloatPropertyConverter());
		setTypeConverter(Double.class, new DoublePropertyConverter());

		setTypeConverter(String.class, PropertyConverter.identity());
	}

	public static <T> void setTypeConverter(Class<T> type, PropertyConverter<T> converter) {
		typeMappings.put(type, converter);
	}

	public static <T> T convert(Class<T> type, Key key, String value) {
		PropertyConverter<?> conversion = typeMappings.get(type);
		if (conversion == null) {
			throw new MissingTypeConverterException(type, value);
		}
		return (T) conversion.apply(key.value(), value);
	}
}
