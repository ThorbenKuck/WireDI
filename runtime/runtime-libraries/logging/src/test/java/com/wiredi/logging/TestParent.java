package com.wiredi.logging;

public class TestParent {

    private final Logging logging;

    public TestParent(LoggingAccessor loggingAccessor) {
        this.logging = loggingAccessor.get(TestParent.class);
    }

    public void foo() {
        logging.info(() -> "Hello");
    }
}
