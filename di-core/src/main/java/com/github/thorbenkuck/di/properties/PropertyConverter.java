package com.github.thorbenkuck.di.properties;

public interface PropertyConverter<T> {

    T apply(TypedProperties typedProperties, String key, String defaultValue);

}
