package com.wiredi.runtime;

import org.junit.jupiter.api.Test;

class ApplicationTest {
    @Test
    public void applicationStartCan() {
        Application app = Application.start();
        app.awaitCompletion();
    }
}