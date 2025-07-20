package com.wiredi.tests;

import com.wiredi.runtime.WireContainer;
import org.junit.jupiter.api.extension.ExtensionContext;

public interface TestCallback {

    default void beforeAll(ExtensionContext context, WireContainer wireContainer) throws Exception {
    }

    default void beforeEach(ExtensionContext context, WireContainer wireContainer) throws Exception {
    }

    default void afterEach(ExtensionContext context, WireContainer wireContainer) throws Exception {
    }

    default void afterAll(ExtensionContext context, WireContainer wireContainer) throws Exception {
    }
}
