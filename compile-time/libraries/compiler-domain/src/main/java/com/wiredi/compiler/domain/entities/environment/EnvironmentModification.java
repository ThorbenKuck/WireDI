package com.wiredi.compiler.domain.entities.environment;

import com.squareup.javapoet.CodeBlock;
import com.wiredi.runtime.properties.Key;

public interface EnvironmentModification {

    void apply(CodeBlock.Builder builder);

    static EnvironmentModification addProperty(Key key, String value) {
        return new AddPropertyEnvironmentModification(key, value);
    }

    static EnvironmentModification setProperty(Key key, String value) {
        return new SetPropertyEnvironmentModification(key, value);
    }

    static EnvironmentModification addActiveProfile(String value) {
        return new AddActiveProfileEnvironmentModification(value);
    }
}
