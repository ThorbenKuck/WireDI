package com.wiredi.runtime.types.converter;

import com.wiredi.runtime.types.AbstractTypeConverter;
import com.wiredi.runtime.types.Bytes;

import java.nio.ByteBuffer;
import java.util.List;

public class ShortTypeConverter extends AbstractTypeConverter<Short> {

	public static final ShortTypeConverter INSTANCE = new ShortTypeConverter();

	public ShortTypeConverter() {
		super(List.of(Short.class, short.class));
	}

	@Override
	protected void setup() {
		register(String.class, Short::parseShort);
		register(byte[].class, Bytes::toShort);

		register(long.class, Long::shortValue);
		register(int.class, Integer::shortValue);
		register(short.class, Short::shortValue);
		register(float.class, Float::shortValue);
		register(double.class, Double::shortValue);

		register(Long.class, Long::shortValue);
		register(Integer.class, Integer::shortValue);
		register(Short.class, Short::shortValue);
		register(Float.class, Float::shortValue);
		register(Double.class, Double::shortValue);
	}
}
