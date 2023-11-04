package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.wiredi.annotations.properties.Name;
import com.wiredi.annotations.properties.Property;
import com.wiredi.annotations.properties.PropertyBinding;
import com.wiredi.compiler.domain.*;
import com.wiredi.compiler.errors.ProcessingException;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.environment.Environment;
import com.wiredi.properties.TypedProperties;
import com.wiredi.properties.keys.Key;
import com.wiredi.runtime.WireRepository;
import jakarta.inject.Named;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CreateInstanceForPropertyBindingMethod extends CreateInstanceMethodFactory {

    private static final Logger logger = Logger.get(CreateInstanceForPropertyBindingMethod.class);
    private final PropertyBinding annotation;
    private final TypeElement typeElement;
    private final Environment environment;

    public CreateInstanceForPropertyBindingMethod(
            PropertyBinding annotation,
            TypeElement typeElement,
            WireRepositories wireRepositories,
            CompilerRepository compilerRepository,
            Environment environment) {
        super(compilerRepository, wireRepositories);
        this.annotation = annotation;
        this.typeElement = typeElement;
        this.environment = environment;
    }

    @Override
    public void append(
            MethodSpec.Builder builder,
            ClassEntity<?> entity
    ) {
        CodeBlock instantiation;
        if (annotation.lifecycle() == PropertyBinding.Lifecycle.RUNTIME) {
            instantiation = runtime(entity, annotation.prefix(), annotation.file());
        } else if (annotation.lifecycle() == PropertyBinding.Lifecycle.COMPILE) {
            instantiation = compileTime(entity, annotation.prefix(), annotation.file());
        } else {
            throw new IllegalStateException("Unknown PropertyBinding Lifecycle " + annotation.lifecycle());
        }

        builder.returns(TypeName.get(entity.rootType()))
                .addModifiers(Modifier.PRIVATE)
                .addParameter(WireRepository.class, "wireRepository", Modifier.FINAL)
                .addCode(instantiation)
                .build();
    }

    private CodeBlock runtime(
            ClassEntity<?> entity,
            String prefix,
            String file
    ) {
        List<PropertyInjectionPoint> injectionPoints = constructorInjectionPoints(prefix);
        CodeBlock.Builder builder = CodeBlock.builder();

        if (!file.isBlank()) {
            return builder.beginControlFlow("try ($T properties = wireRepository.environment().loadProperties($S))", TypedProperties.class, file)
                    .add("return new $T(\n", entity.rootType())
                    .indent()
                    .add("$L\n", asParameterList(CodeBlock.of("properties.require"), injectionPoints))
                    .unindent()
                    .addStatement(")")
                    .endControlFlow()
                    .build();
        }

        return builder.add("return new $T(\n", entity.rootType())
                .indent()
                .add("$L\n", asParameterList(CodeBlock.of("wireRepository.environment().getProperty"), injectionPoints))
                .unindent()
                .addStatement(")")
                .build();

    }

    private CodeBlock compileTime(
            ClassEntity<?> entity,
            String prefix,
            String file
    ) {
        List<PropertyInjectionPoint> injectionPoints = constructorInjectionPoints(prefix);
        CodeBlock.Builder builder = CodeBlock.builder();

        if (!file.isBlank()) {
            try (TypedProperties properties = environment.loadProperties(file)) {
                List<CodeBlock> parameters = injectionPoints.stream()
                        .map(it -> CodeBlock.of("$S", properties.get(it.key)))
                        .collect(Collectors.toList());

                return builder.add("return new $T(\n", entity.rootType())
                        .indent()
                        .add("$L\n", CodeBlock.join(parameters, ",\n"))
                        .unindent()
                        .addStatement(")")
                        .build();
            }
        }

        throw new ProcessingException(typeElement, "The CompileTime lifecycle requires a file to be provided");
    }

    private CodeBlock asParameterList(CodeBlock root, List<PropertyInjectionPoint> injectionPoints) {
        return CodeBlock.join(
                injectionPoints.stream()
                        .map(point -> getProperty(
                                root,
                                point.key(),
                                point.defaultValue())
                        )
                        .toList(),
                "," + System.lineSeparator()
        );
    }

    private CodeBlock getProperty(CodeBlock root, Key key, String defaultValue) {
        if (defaultValue == null || defaultValue.isBlank()) {
            return getProperty(root, key);
        }
        return CodeBlock.builder().add("$L($T.just($S), $S)", root, Key.class, key.value(), defaultValue).build();
    }

    private CodeBlock getProperty(CodeBlock root, Key key) {
        return CodeBlock.builder().add("$L($T.just($S))", root, Key.class, key.value()).build();
    }


    private List<PropertyInjectionPoint> constructorInjectionPoints(String prefix) {
        return TypeUtils.findPrimaryConstructor(typeElement)
                .map(it -> findAllIn(prefix, it))
                .orElse(Collections.emptyList());
    }

    private List<PropertyInjectionPoint> findAllIn(String prefix, ExecutableElement constructor) {
        List<? extends Element> elements = constructor.getParameters();
        return elements.stream()
                .map(element -> new PropertyInjectionPoint(
                        propertyName(element).withPrefix(prefix),
                        defaultValue(element), element)
                ).toList();
    }

    private Key propertyName(Element element) {
        return Annotations.getAnnotation(element, Property.class)
                .map(Property::name)
                .or(() -> Annotations.getAnnotation(element, Name.class).map(Name::value))
                .or(() -> Annotations.getAnnotation(element, Named.class).map(Named::value))
                .or(() -> Optional.of(element.getSimpleName().toString()))
                .map(Key::format)
                .get();
    }

    private String defaultValue(Element element) {
        return Annotations.getAnnotation(element, Property.class)
                .map(Property::defaultValue)
                .orElse(null);
    }

    record PropertyInjectionPoint(@NotNull Key key, @Nullable String defaultValue, @NotNull Element element) {

    }
}
