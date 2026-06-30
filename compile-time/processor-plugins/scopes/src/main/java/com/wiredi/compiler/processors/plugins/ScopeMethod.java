package com.wiredi.compiler.processors.plugins;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;
import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.ScopeProvider;
import com.wiredi.runtime.domain.scopes.SingletonScope;
import com.wiredi.runtime.scope.ScopeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;

public class ScopeMethod implements StandaloneMethodFactory {

    private static final Logging logger = Logging.getInstance(ScopeMethod.class);
    @NotNull
    private final List<@NotNull ScopeType> scopeTypes;
    @NotNull
    private final Types types;

    public ScopeMethod(
            @NotNull List<@NotNull ScopeType> scopeTypes,
            @NotNull Types types
    ) {
        this.types = types;
        if (scopeTypes.isEmpty()) {
            throw new IllegalArgumentException("Scope types cannot be empty");
        }
        this.scopeTypes = scopeTypes;
    }

    @Override
    public @NotNull String methodName() {
        return "scope";
    }

    @Override
    public void append(MethodSpec.@NotNull Builder builder, @NotNull ClassEntity<?> entity) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        if (scopeTypes.size() == 1) {
            ScopeType scopeType = scopeTypes.getFirst();
            ScopeType.Metadata metadata = scopeType.metadata();
            if (metadata != null) {
                TypeMirror providerType = metadata.scopeProvider();
                if (providerType != null) {
                    codeBlock.add(withCustomProvider((TypeElement) types.asElement(providerType)));
                } else {
                    codeBlock.add(withCustomScopeInitializer(scopeType, metadata.scopeInitializer()));
                }
            } else {
                codeBlock.add(defaultProvider(scopeType));
            }
        }

        builder.returns(ScopeProvider.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Nullable.class)
                .addCode(codeBlock.build());
    }

    private CodeBlock withCustomScopeInitializer(ScopeType scopeType, String initializer) {
        return CodeBlock.builder().add("return $T.forIdentifier($T.class)\n", ScopeProvider.class, TypeName.get(scopeType.scopeAnnotation().getAnnotationType()))
                .indent()
                .add(".withScope($L)\n", initializer)
                .addStatement(".build()")
                .unindent()
                .build();
    }

    private CodeBlock withCustomProvider(TypeElement providerType) {
        return CodeBlock.builder()
                .addStatement("return new $T()", ClassName.get(providerType))
                .build();
    }

    private CodeBlock defaultProvider(ScopeType scopeType) {
        return CodeBlock.builder().add("return $T.forIdentifier($T.class)\n", ScopeProvider.class, TypeName.get(scopeType.scopeAnnotation().getAnnotationType()))
                .indent()
                .add(".withScope($T.SUPPLIER)\n", SingletonScope.class)
                .addStatement(".build()")
                .unindent()
                .build();
    }
}
