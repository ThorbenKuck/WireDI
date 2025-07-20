package com.wiredi.tests;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.lang.ThrowingBiConsumer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;

public abstract class AbstractTestExtension implements BeforeEachCallback, AfterEachCallback {

    @NotNull
    public abstract WireContainer wireContainerOf(ExtensionContext context);

    public void beforeAll(ExtensionContext context) throws Exception {
        executeOnAllCallbacks(context, (testCallback, wireContainer) -> testCallback.beforeAll(context, wireContainer));
    }

    public void beforeEach(ExtensionContext context) throws Exception {
        executeOnAllCallbacks(context, (testCallback, wireContainer) -> testCallback.beforeEach(context, wireContainer));
    }

    public void afterEach(ExtensionContext context) throws Exception {
        executeOnAllCallbacks(context, (testCallback, wireContainer) -> testCallback.afterEach(context, wireContainer));
    }

    public void afterAll(ExtensionContext context) throws Exception {
        executeOnAllCallbacks(context, (testCallback, wireContainer) -> testCallback.afterAll(context, wireContainer));
    }

    private void executeOnAllCallbacks(ExtensionContext context, ThrowingBiConsumer<TestCallback, WireContainer, Exception> callbackConsumer) throws Exception {
        WireContainer wireContainer = wireContainerOf(context);
        List<TestCallback> callbacks = wireContainer.getAll(TestCallback.class);
        for (TestCallback callback : callbacks) {
            callbackConsumer.accept(callback, wireContainer);
        }
    }
}
