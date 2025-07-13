package com.wiredi.compiler.domain.injection;

import com.squareup.javapoet.CodeBlock;
import com.wiredi.annotations.environment.Resolve;
import com.wiredi.annotations.properties.Property;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.Qualifiers;
import com.wiredi.compiler.domain.WireRepositories;
import com.wiredi.runtime.lang.TriConsumer;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class VariableContext {

    @NotNull
    private final NameContext nameContext = new NameContext();

    @NotNull
    private final String varPrefix;

    @NotNull
    private final Map<TypeMirror, Map<QualifierType, String>> qualifiedCache = new HashMap<>();

    @NotNull
    private final Map<String, String> resolveCache = new HashMap<>();

    public VariableContext(@NotNull String varPrefix) {
        this.varPrefix = varPrefix;
    }

    public VariableContext() {
        this("variable");
    }

    public String instantiateVariableIfRequired(
            @NotNull VariableElement element,
            @NotNull WireRepositories wireRepositories,
            @NotNull CodeBlock.Builder codeBlock
    ) {
        return instantiateVariableIfRequired(
                element,
                (name, qualifier) -> codeBlock.addStatement("$T $L = $L", element.asType(), name, wireRepositories.fetchFromWireRepository(element, qualifier)),
                (name, resolve) -> codeBlock.addStatement("$T $L = $L", element.asType(), name, wireRepositories.resolveFromEnvironment(element, resolve)),
                (name, propertyName, defaultValue) -> codeBlock.addStatement("$T $L = $L", element.asType(), name, wireRepositories.fetchFromProperties(element, propertyName, defaultValue))
        );
    }

    public String instantiateVariableIfRequired(
            @NotNull Element element,
            @NotNull BiConsumer<String, QualifierType> newNameFunction,
            @NotNull BiConsumer<String, String> newResolveFunction,
            @NotNull TriConsumer<String, String, String> newPropertyFunction
    ) {
        return instantiateForResolve(element, newResolveFunction)
                .or(() -> instantiateForProperty(element, newPropertyFunction))
                .orElseGet(() -> instantiateVariableIfRequired(element, newNameFunction));
    }

    private Optional<String> instantiateForResolve(Element element, BiConsumer<String, String> newResolveFunction) {
        return Annotations.getAnnotation(element, Resolve.class)
                .map(annotation -> resolveCache.computeIfAbsent(annotation.value(), (value) -> {
                    String nextName = nameContext.nextName(varPrefix);
                    newResolveFunction.accept(nextName, value);
                    return nextName;
                }));
    }

    public Optional<String> instantiateForProperty(Element element, TriConsumer<String, String, String> newPropertyFunction) {
        return Annotations.getAnnotation(element, Property.class)
                .map(annotation -> resolveCache.computeIfAbsent(annotation.name(), (value) -> {
                    String nextName = nameContext.nextName(varPrefix);
                    newPropertyFunction.accept(nextName, value, annotation.defaultValue());
                    return nextName;
                }));
    }

    /**
     * Allows for single instantiations of variables and therefore reusing existing variables.
     * <p>
     * The neNameFunction will be invoked if a new variable name is created.
     *
     * @param element         the element for what a variable should be instantiated
     * @param newNameFunction the function to invoke if a new variable is created
     * @return the name associated with the typeMirror
     */
    public String instantiateVariableIfRequired(
            @NotNull Element element,
            @NotNull BiConsumer<String, QualifierType> newNameFunction
    ) {
        String nextName = nameContext.nextName(varPrefix);
        newNameFunction.accept(nextName, Qualifiers.injectionQualifier(element));
        return nextName;
    }
}
