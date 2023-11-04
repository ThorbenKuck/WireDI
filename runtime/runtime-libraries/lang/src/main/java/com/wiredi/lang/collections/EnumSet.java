package com.wiredi.lang.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.wiredi.lang.Preconditions.notNull;

public class EnumSet<T extends Enum<T>> {

    private final Map<String, T> content;
    private final Class<T> type;

    private static final TypeMap<EnumSet<?>> sets = new TypeMap<>();

    public static synchronized <T extends Enum<T>> EnumSet<T> of(Class<T> type) {
        return (EnumSet<T>) sets.computeIfAbsent(type, () -> new EnumSet<>(type));
    }

    private EnumSet(Class<T> type) {
        final Map<String, T> targetContents = new HashMap<>();
        for (T constant : type.getEnumConstants()) {
            targetContents.put(constant.name(), constant);
        }

        this.content = Collections.unmodifiableMap(targetContents);
        this.type = type;
    }

    public Optional<T> get(String name) {
        return Optional.ofNullable(content.get(name));
    }

    public T require(String name) {
        return notNull(content.get(name), () -> "The enum of type " + type + " has no enum with the name " + name);
    }

    public void forEach(Consumer<? super T> consumer) {
        content.values().forEach(consumer);
    }

    public void forEachNamed(BiConsumer<String, ? super T> consumer) {
        content.forEach(consumer);
    }

    public Class<T> type() {
        return type;
    }
}
