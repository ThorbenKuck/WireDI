package com.wiredi.compiler.logger.slf4j;

public interface LoggerRegistry<T> {
    void setDefault(T value);

    void set(String pattern, T value);

    T get(String name);
}
