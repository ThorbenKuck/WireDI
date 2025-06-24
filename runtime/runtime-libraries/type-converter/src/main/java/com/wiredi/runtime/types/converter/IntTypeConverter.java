package com.wiredi.runtime.types.converter;

import com.wiredi.runtime.types.TypeConverterBase;
import com.wiredi.runtime.types.Bytes;

import java.util.List;

public class IntTypeConverter extends TypeConverterBase<Integer> {

	public static final IntTypeConverter INSTANCE = new IntTypeConverter();

	public IntTypeConverter() {
		super(List.of(Integer.class, int.class));
	}

	@Override
	protected void setup() {
		register(String.class, Integer::parseInt);
		register(byte[].class, Bytes::toInt);

		register(long.class, Long::intValue);
		register(short.class, Short::intValue);
		register(float.class, Float::intValue);
		register(double.class, Double::intValue);

		register(Long.class, Long::intValue);
		register(Short.class, Short::intValue);
		register(Float.class, Float::intValue);
		register(Double.class, Double::intValue);
	}
}
