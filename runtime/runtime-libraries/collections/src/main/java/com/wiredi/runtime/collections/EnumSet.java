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
 * A fast name-to-enum lookup helper with O(1) access by name and a case-insensitive variant.
 * It precomputes a map from the enum constant names to the constants themselves and also stores a
 * simplified form of each name for case-insensitive lookups. Instances are cached per enum type, so
 * subsequent calls to {@link #of(Class)} are cheap and return a shared instance.
 * <p>
 * Example:
 * <pre>{@code
 * enum Status { NEW_ORDER, IN_PROGRESS, DONE }
 *
 * EnumSet<Status> set = EnumSet.of(Status.class);
 * set.get("IN_PROGRESS").ifPresent(s -> System.out.println(s)); // IN_PROGRESS
 * set.getIgnoreCase("in-progress").ifPresent(s -> System.out.println(s)); // IN_PROGRESS
 *
 * // Required access throws if missing:
 * Status value = set.require("DONE");
 * }
 * </pre>
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

    /**
     * Builds an EnumSet for the given enum type by indexing all constants by name and simplified name.
     */
    public EnumSet(@NotNull final Class<T> type) {
        @NotNull final Map<String, T> targetContents = new HashMap<>();
        for (@NotNull final T constant : type.getEnumConstants()) {
            targetContents.put(constant.name(), constant);
            targetContents.put(simplifyEnumName(constant.name()), constant);
        }

        this.content = Collections.unmodifiableMap(targetContents);
        this.type = type;
    }

    /**
     * Simplifies an enum name by lowercasing and replacing underscores with dashes.
     * This is used to support case-insensitive lookups with common kebab-case inputs.
     */
    public static String simplifyEnumName(String name) {
        return name.replace('_', '-').toLowerCase();
    }

    /**
     * Returns a cached EnumSet for the given enum type. One instance is maintained per enum class.
     * @throws IllegalArgumentException if the provided class is not an enum
     */
    @NotNull
    public static <T extends Enum<T>> EnumSet<T> of(@NotNull final Class<T> type) {
        Preconditions.is(type.isEnum(), () -> "The provided type " + type + " is not an enum");
        return (EnumSet<T>) sets.computeIfAbsent(type, () -> new EnumSet<>(type));
    }

    /**
     * Returns the constant for the exact name if present.
     */
    @NotNull
    public Optional<T> get(@NotNull final String name) {
        return Optional.ofNullable(content.get(name));
    }

    /**
     * Returns the constant for the provided name in a case-insensitive manner.
     * Underscores are treated as dashes ("IN_PROGRESS" == "in-progress").
     */
    @NotNull
    public Optional<T> getIgnoreCase(@NotNull final String name) {
        return Optional.ofNullable(content.get(simplifyEnumName(name)));
    }

    /**
     * Returns the constant for the exact name or throws if not present.
     * @throws NullPointerException if no constant with the given name exists
     */
    @NotNull
    public T require(@NotNull String name) {
        return isNotNull(content.get(name), () -> "The enum of type " + type + " has no enum with the name " + name);
    }

    /**
     * Returns the constant for the name using case-insensitive matching or throws if not present.
     * @throws NullPointerException if no matching constant exists
     */
    @NotNull
    public T requireIgnoreCase(@NotNull String name) {
        return isNotNull(content.get(simplifyEnumName(name)), () -> "The enum of type " + type + " has no enum with the name " + name);
    }

    /** Iterates over all enum constants. */
    public void forEach(Consumer<@NotNull T> consumer) {
        content.values().forEach(consumer);
    }

    /** Iterates over name-constant pairs, including simplified name entries. */
    public void forEachNamed(BiConsumer<@NotNull String, @NotNull T> consumer) {
        content.forEach(consumer);
    }

    /** Returns the underlying enum type. */
    @NotNull
    public Class<T> type() {
        return type;
    }
}
