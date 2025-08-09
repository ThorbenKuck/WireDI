package com.wiredi.test;

import java.util.HashMap;
import java.util.Map;

public class ConditionChecker {

    private final Map<String, ConditionTest> conditionTestMap = new HashMap<>();

    public ConditionChecker expectInvocation(String conditionName) {
        conditionTestMap.put(conditionName, ConditionTest.failureOnDefault());
        return this;
    }

    public ConditionChecker expectNoInvocation(String conditionName) {
        conditionTestMap.put(conditionName, ConditionTest.successOnDefault());
        return this;
    }

    public void invoked(String conditionName) {
        ConditionTest conditionTest = conditionTestMap.get(conditionName);
        if (conditionTest == null) {
            conditionTestMap.put(conditionName, ConditionTest.failure());
        } else {
            conditionTest.switchState();
        }
    }

    private static final class ConditionTest {

        public static ConditionTest successOnDefault() {
            return new ConditionTest(true);
        }

        public static ConditionTest failureOnDefault() {
            return new ConditionTest(false);
        }

        public static ConditionTest success() {
            return failureOnDefault().switchState();
        }

        public static ConditionTest failure() {
            return failureOnDefault().switchState();
        }

        private boolean success;
        private boolean invoked = false;

        public ConditionTest(boolean success) {
            this.success = success;
        }

        public ConditionTest switchState() {
            if (invoked) {
                this.success = false;
            } else {
                this.success = !this.success;
                invoked = true;
            }

            return this;
        }
    }
}
