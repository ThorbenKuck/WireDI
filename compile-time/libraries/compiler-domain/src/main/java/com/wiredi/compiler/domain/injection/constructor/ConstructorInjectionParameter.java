package com.wiredi.compiler.domain.injection.constructor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

public record ConstructorInjectionParameter(
        @NotNull VariableElement parameter,
        @Nullable VariableElement backingField,
        @Nullable ExecutableElement backingSetter
) {
}
