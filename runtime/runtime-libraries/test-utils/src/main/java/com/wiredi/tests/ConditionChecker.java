package com.wiredi.tests;

import java.util.HashMap;
import java.util.Map;

public class ConditionChecker {

    private final Map<String, ConditionTest> conditionTestMap = new HashMap<>();

    private static final class ConditionTest {
        private boolean success;
        private boolean invoked = false;

        public void switchState() {
            if (invoked) {
                this.success = false;
            } else {
                this.success = !this.success;
                invoked = true;
            }
        }
    }
}
