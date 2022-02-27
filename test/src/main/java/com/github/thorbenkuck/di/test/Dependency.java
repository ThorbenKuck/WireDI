package com.github.thorbenkuck.di.test;

import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.test.commands.Command;
import com.github.thorbenkuck.di.test.commands.CommandA;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Wire(to = IDependency.class)
class Dependency implements IDependency {

    private final String id = UUID.randomUUID().toString();

    Dependency() {
        System.out.println("[CONSTRUCTOR   |Dependency] called");
    }

    @PostConstruct
    public void constructed() {
        System.out.println("[POST_CONSTRUCT|Dependency] called");
    }

    @Inject
    void inject(CommandA command) {

    }

    @Inject
    private void doInject(CommandA command) {

    }

    @Override
    public String id() {
        return "Dependency{" + id + "}";
    }
}
