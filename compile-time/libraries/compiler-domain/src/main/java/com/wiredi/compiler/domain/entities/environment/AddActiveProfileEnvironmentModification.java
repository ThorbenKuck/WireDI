package com.wiredi.compiler.domain.entities.environment;

import com.squareup.javapoet.CodeBlock;
import com.wiredi.runtime.properties.Key;

public class AddActiveProfileEnvironmentModification implements EnvironmentModification {

    private final String value;

    public AddActiveProfileEnvironmentModification(String value) {
        this.value = value;
    }

    @Override
    public void apply(CodeBlock.Builder builder) {
        builder.addStatement("environment.addActiveProfile($S)", value);
    }
}
