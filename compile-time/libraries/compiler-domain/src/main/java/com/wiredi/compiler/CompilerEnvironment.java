package com.wiredi.compiler;

import com.wiredi.runtime.Environment;

public class CompilerEnvironment {

    private static Environment environment;

    static {
        System.setProperty("compiler.logging.level", "DEBUG");
    }

    public static synchronized Environment get() {
        if (environment == null) {
            environment = new Environment();
            environment.autoconfigure();
        }

        environment.properties().forEach(entry -> System.out.println(entry.getKey() + " = " + entry.getValue()));

        return environment;
    }

}
