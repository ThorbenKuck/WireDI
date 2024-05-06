package com.wiredi.runtime.properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A ThreadLocal state of TypedProperties.
 * <p>
 * If constructed using the default constructor, a new TypedProperties is constructed for each thread.
 * <p>
 * Contrary you can provide a TypedProperties instance to this. If you do, each thread will receive a new instance
 * that is pre-filled with all values of the TypedProperties.
 * In this scenario, it is important to note that this ThreadLocal class will take the current state of the
 * TypedProperties as they are passed by reference.
 * <p>
 * ThreadLocal instances with predefined values should be used with care, as the provided TypedProperties is passed
 * by reference and not normally freed for the GC. Instead, it is recommended to provide a custom Supplier.
 */
public final class ThreadLocalTypedProperties extends ThreadLocal<TypedProperties> {

    @NotNull
    private final Supplier<@Nullable TypedProperties> supplier;
    private final boolean copyInstance;

    public ThreadLocalTypedProperties(
            @NotNull final Supplier<@Nullable TypedProperties> supplier,
            final boolean copyInstance
    ) {
        this.supplier = supplier;
        this.copyInstance = copyInstance;
    }

    public ThreadLocalTypedProperties(
            @NotNull final Supplier<@Nullable TypedProperties> supplier
    ) {
        this(supplier, false);
    }

    /**
     * Will construct a new ThreadLocalTypedProperties that references the provide {@link TypedProperties base}
     *
     * Each thread will receive a copied instance of the supplied property.
     *
     * @param base the base class for each
     */
    public ThreadLocalTypedProperties(
            @Nullable final TypedProperties base
    ) {
        this(() -> base, true);
    }

    /**
     * Constructs a new ThreadLocalTypedProperties instance.
     * <p>
     * Each Thread will receive a new instance of TypedProperties.
     */
    public ThreadLocalTypedProperties() {
        this(TypedProperties::new, false);
    }

    @Override
    protected TypedProperties initialValue() {
        final TypedProperties base = supplier.get();
        if (base == null) {
            return null;
        }

        if (copyInstance) {
            return base.copy();
        } else {
            return base;
        }
    }
}
