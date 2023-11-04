package com.wiredi.lang.values;

public interface IfPresentStage {
    static IfPresentStage wasPresent() {
        return ValuePresentIfPresentStage.INSTANCE;
    }

    static IfPresentStage wasMissing() {
        return ValueMissingIfPresentStage.INSTANCE;
    }

    void orElse(Runnable runnable);

    class ValueMissingIfPresentStage implements IfPresentStage {

        private static final IfPresentStage INSTANCE = new ValueMissingIfPresentStage();

        @Override
        public void orElse(Runnable runnable) {
            runnable.run();
        }
    }

    class ValuePresentIfPresentStage implements IfPresentStage {

        private static final IfPresentStage INSTANCE = new ValuePresentIfPresentStage();

        @Override
        public void orElse(Runnable runnable) {
            // Ignore
        }
    }
}


