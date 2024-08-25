package com.wiredi.runtime.collections;

import com.wiredi.runtime.lang.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.wiredi.runtime.lang.Preconditions.isNotNull;

/**
 * An EnumSet is a set holding enum values.
 * <p>
 * This class associates the name of enum values to their concrete value.
 * This reduces the lookup time for names in enums from O(n) to O(1), because it uses the hash of the name
 * to look up the value.
 * <p>
 * When constructing an {@link EnumSet} using {@link #of(Class)}, it will cache the constructed value, so that
 * only one instance of an EnumSet can exist per enum type.
 *
 * @param <T> the enum type
 */
public final class EnumSet<T extends Enum<T>> {

    @NotNull
    private static final TypeMap<EnumSet<?>> sets = new TypeMap<>();
    @NotNull
    private final Map<String, T> content;
    @NotNull
    private final Class<T> type;

    public EnumSet(@NotNull final Class<T> type) {
        @NotNull final Map<String, T> targetContents = new HashMap<>();
        for (@NotNull final T constant : type.getEnumConstants()) {
            targetContents.put(constant.name(), constant);
            targetContents.put(simplifyEnumName(constant.name()), constant);
        }

        this.content = Collections.unmodifiableMap(targetContents);
        this.type = type;
    }

    public static String simplifyEnumName(String name) {
        return name.replace('_', '-').toLowerCase();
    }

    @NotNull
    public static <T extends Enum<T>> EnumSet<T> of(@NotNull final Class<T> type) {
        Preconditions.is(type.isEnum(), () -> "The provided type " + type + " is not an enum");
        return (EnumSet<T>) sets.computeIfAbsent(type, () -> new EnumSet<>(type));
    }

    @NotNull
    public Optional<T> get(@NotNull final String name) {
        return Optional.ofNullable(content.get(name));
    }

    @NotNull
    public Optional<T> getIgnoreCase(@NotNull final String name) {
        return Optional.ofNullable(content.get(simplifyEnumName(name)));
    }

    @NotNull
    public T require(@NotNull String name) {
        return isNotNull(content.get(name), () -> "The enum of type " + type + " has no enum with the name " + name);
    }

    @NotNull
    public T requireIgnoreCase(@NotNull String name) {
        return isNotNull(content.get(simplifyEnumName(name)), () -> "The enum of type " + type + " has no enum with the name " + name);
    }

    public void forEach(Consumer<@NotNull T> consumer) {
        content.values().forEach(consumer);
    }

    public void forEachNamed(BiConsumer<@NotNull String, @NotNull T> consumer) {
        content.forEach(consumer);
    }

    @NotNull
    public Class<T> type() {
        return type;
    }
}
