package com.wiredi.runtime.types.converter;

import com.wiredi.runtime.types.TypeConverterBase;
import com.wiredi.runtime.types.exceptions.TypeConversionFailedException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassTypeConverter extends TypeConverterBase<Class> {

    public static ClassTypeConverter INSTANCE = new ClassTypeConverter();
    private static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>(10);

    public ClassTypeConverter() {
        super(Class.class);
    }

    @Override
    protected void setup() {
        register(String.class, prop -> classCache.computeIfAbsent(prop, name -> {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new TypeConversionFailedException(getTargetTypes(), name, e);
            }
        }));
    }
}
