package com.wiredi.tests;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.WiredApplicationInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Objects;

public class WireContainerContext {

    /**
     * A utility function to determine the WireContainer of the current {@link ExtensionContext}.
     * <p>
     * This can be used in custom extensions to determine related containers.
     *
     * @param context the context to analyze
     * @return a {@link WireContainer} initialized for this context, or null if non is initialized
     */
    @Nullable
    public static WireContainer determineWireContainer(ExtensionContext context) {
        WiredApplicationInstance application = ApplicationTestExtension.getApplicationInstance(context);
        if (application != null) {
            return application.wireContainer();
        }

        return WiredTestExtension.getWireContainer(context);
    }

    /**
     * A utility function to determine the WireContainer of the current {@link ExtensionContext}.
     * <p>
     * As an extension to {@link #determineWireContainer(ExtensionContext)}, this function will throw an error is the container is not set for the current context.
     *
     * @param context the context to analyze
     * @return a {@link WireContainer} initialized for this context; never {@code null}
     */
    @NotNull
    public static WireContainer getWireContainer(ExtensionContext context) {
        return Objects.requireNonNull(determineWireContainer(context), "The WireContainer is not initialized for the current context");
    }
}
