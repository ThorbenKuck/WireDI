package com.wiredi.test;

import com.wiredi.annotations.Wire;
import com.wiredi.annotations.Provider;
import com.wiredi.test.commands.CommandA;
import com.wiredi.test.commands.CommandB;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.util.UUID;

@Wire(to = IDependency.class)
class Dependency implements IDependency {

    private final String id = UUID.randomUUID().toString();
    private final ConditionChecker conditionChecker = new ConditionChecker();

    Dependency() {
        System.out.println("[CONSTRUCTOR   |Dependency] called");
    }

    @PostConstruct
    public void constructed() {
        System.out.println("[POST_CONSTRUCT|Dependency] called");
    }

    @Inject
    void inject(CommandA command) {
        System.out.println("");
    }

    @Inject
    private void doInject(CommandA command) {

    }

    @Override
    public String id() {
        return "Dependency{" + id + "}";
    }

    @Provider
    public String example(CommandB commandB) {
        return "Foo";
    }
}
