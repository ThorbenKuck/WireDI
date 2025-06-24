package com.wiredi.runtime.types.converter;

import com.wiredi.runtime.types.TypeConverterBase;
import com.wiredi.runtime.types.Bytes;

import java.util.List;

public class LongTypeConverter extends TypeConverterBase<Long> {

	public static final LongTypeConverter INSTANCE = new LongTypeConverter();

	public LongTypeConverter() {
		super(List.of(Long.class, long.class));
	}

	@Override
	protected void setup() {
		register(String.class, Long::parseLong);
		register(byte[].class, Bytes::toLong);

		register(int.class, Integer::longValue);
		register(long.class, Long::longValue);
		register(short.class, Short::longValue);
		register(float.class, Float::longValue);
		register(double.class, Double::longValue);

		register(Integer.class, Integer::longValue);
		register(Long.class, Long::longValue);
		register(Short.class, Short::longValue);
		register(Float.class, Float::longValue);
		register(Double.class, Double::longValue);
	}
}
