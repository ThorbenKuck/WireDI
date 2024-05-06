package com.wiredi.runtime.values;

public sealed interface IfPresentStage {
    static IfPresentStage wasPresent() {
        return ValuePresentIfPresentStage.INSTANCE;
    }

    static IfPresentStage wasMissing() {
        return ValueMissingIfPresentStage.INSTANCE;
    }

    void orElse(Runnable runnable);

    final class ValueMissingIfPresentStage implements IfPresentStage {

        private static final IfPresentStage INSTANCE = new ValueMissingIfPresentStage();

        @Override
        public void orElse(Runnable runnable) {
            runnable.run();
        }
    }

    final class ValuePresentIfPresentStage implements IfPresentStage {

        private static final IfPresentStage INSTANCE = new ValuePresentIfPresentStage();

        @Override
        public void orElse(Runnable runnable) {
            // Ignore
        }
    }
}


