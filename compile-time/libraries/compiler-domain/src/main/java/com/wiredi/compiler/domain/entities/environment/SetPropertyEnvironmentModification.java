package com.wiredi.compiler.domain.entities.environment;

import com.squareup.javapoet.CodeBlock;
import com.wiredi.runtime.properties.Key;

public class SetPropertyEnvironmentModification implements EnvironmentModification {

    private final Key key;
    private final String value;

    public SetPropertyEnvironmentModification(Key key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public void apply(CodeBlock.Builder builder) {
        builder.addStatement("environment.properties().set($T.just($S), $S)", Key.class, key.value(), value);
    }
}
