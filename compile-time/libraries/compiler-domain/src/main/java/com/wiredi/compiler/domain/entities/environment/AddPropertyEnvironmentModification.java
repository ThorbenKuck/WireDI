package com.wiredi.compiler.domain.entities.environment;

import com.squareup.javapoet.CodeBlock;
import com.wiredi.runtime.properties.Key;

public class AddPropertyEnvironmentModification implements EnvironmentModification {

    private final Key key;
    private final String value;

    public AddPropertyEnvironmentModification(Key key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public void apply(CodeBlock.Builder builder) {
        builder.addStatement("environment.properties().add($T.just($S), $S)", Key.class, key.value(), value);
    }
}
