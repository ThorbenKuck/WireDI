package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.*;
import com.wiredi.annotations.DeprecationLevel;
import com.wiredi.annotations.properties.*;
import com.wiredi.annotations.properties.Name;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.TypeUtils;
import com.wiredi.compiler.domain.WireRepositories;
import com.wiredi.compiler.domain.properties.ItemDeprecation;
import com.wiredi.compiler.domain.properties.PropertyContext;
import com.wiredi.compiler.errors.ProcessingException;
import com.wiredi.compiler.logger.slf4j.CompileTimeLogger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.lang.DynamicBuilder;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.properties.TypedProperties;
import jakarta.inject.Named;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public class CreateInstanceForPropertyBindingMethod extends CreateInstanceMethodFactory {

    private static final CompileTimeLogger logger = CompileTimeLogger.getLogger(CreateInstanceForPropertyBindingMethod.class);
    private final PropertyBinding annotation;
    private final TypeElement typeElement;
    private final Environment environment;
    private final TypeMirror stringType;
    private final TypeMirror collectionType;
    private final PropertyContext propertyContext;

    public CreateInstanceForPropertyBindingMethod(
            PropertyBinding annotation,
            TypeElement typeElement,
            WireRepositories wireRepositories,
            CompilerRepository compilerRepository,
            Environment environment,
            PropertyContext propertyContext
    ) {
        super(compilerRepository, wireRepositories);
        this.annotation = annotation;
        this.typeElement = typeElement;
        this.environment = environment;
        this.propertyContext = propertyContext;
        this.stringType = elements().getTypeElement(String.class.getName()).asType();
        this.collectionType = elements().getTypeElement(Collection.class.getName()).asType();
    }

    private static Optional<Key> tryFindPropertyName(Element element) {
        return Annotations.getAnnotation(element, Property.class)
                .map(property -> {
                    if (property.name().isBlank()) {
                        return null;
                    } else {
                        return property.name();
                    }
                })
                .or(() -> Annotations.getAnnotation(element, Name.class).map(Name::value))
                .or(() -> Annotations.getAnnotation(element, Named.class).map(Named::value))
                .or(() -> Optional.of(element.getSimpleName().toString()))
                .map(Key::format);
    }

    private static Key propertyName(Element element) {
        return tryFindPropertyName(element).orElseThrow(() -> new IllegalArgumentException("Could not determine property name of " + element));
    }

    private static Optional<String> tryFindDefaultValue(Element element) {
        return Annotations.getAnnotation(element, Property.class)
                .map(property -> {
                    if (property.defaultValue().isBlank()) {
                        return null;
                    } else {
                        return property.defaultValue();
                    }
                });
    }

    private static String defaultValue(Element element) {
        return tryFindDefaultValue(element).orElse(null);
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
                .addParameter(ParameterizedTypeName.get(ClassName.get(TypeIdentifier.class), TypeName.get(entity.rootType())), "concreteType", Modifier.FINAL)
                .addCode(instantiation)
                .build();
    }

    private CodeBlock runtime(
            ClassEntity<?> entity,
            String prefix,
            String file
    ) {
        CodeBlock.Builder builder = CodeBlock.builder();

        if (file.isBlank()) {
            builder.addStatement("$T properties = wireRepository.environment().properties()", TypedProperties.class);
        } else {
            builder.beginControlFlow("try ($T properties = wireRepository.environment().loadProperties($S))", TypedProperties.class, file);
        }

        builder.addStatement("return $L", runtimeConstructorInvocation((TypeElement) types().asElement(entity.rootType()), prefix));
        if (!file.isBlank()) {
            builder.endControlFlow();
        }

        return builder.build();
    }

    private <T> T firstNotNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private String getDescription(Element element) {
        Description description = element.getAnnotation(Description.class);
        if (description != null && !description.value().isBlank()) {
            logger.info(() -> "Found description " + description.value() + " for " + element.getEnclosingElement() + "." + element);
            return description.value();
        }

        Property property = element.getAnnotation(Property.class);
        if (property != null && !property.description().isBlank()) {
            logger.info(() -> "Found description " + property.description() + " for " + element.getEnclosingElement() + "." + element);
            return property.description();
        }
        logger.info(() -> "Unable to find description for " + element.getEnclosingElement() + "." + element + " (" + element.getAnnotationMirrors() + ")");
        return elements().getDocComment(element);
    }

    @Nullable
    private String getValue(String value) {
        if (value.isBlank()) {
            return null;
        }

        return value;
    }

    @Nullable
    private ItemDeprecation deprecation(Element element) {
        Deprecated deprecated = element.getAnnotation(Deprecated.class);
        if (deprecated != null) {
            return new ItemDeprecation(
                    null,
                    null,
                    getValue(deprecated.since()),
                    DeprecationLevel.WARNING.getValue()
            );
        }

        DeprecatedProperty deprecatedProperty = element.getAnnotation(DeprecatedProperty.class);
        if (deprecatedProperty != null) {
            return new ItemDeprecation(
                    getValue(deprecatedProperty.reason()),
                    getValue(deprecatedProperty.replacement()),
                    getValue(deprecatedProperty.since()),
                    deprecatedProperty.level().getValue()
            );
        }

        return null;
    }

    private CodeBlock runtimeConstructorInvocation(
            TypeElement typeElement,
            String prefix
    ) {
        List<PropertyInjectionPoint> injectionPoints = constructorInjectionPoints(typeElement, prefix);
        List<FieldWithSetter> allFieldsWithSetters = findAllFieldsWithSetters(typeElement);
        CodeBlock.Builder builder = CodeBlock.builder();

        logger.info(() -> "Setting up runtime constructor invocation for property injections");

        for (PropertyInjectionPoint injectionPoint : injectionPoints) {
            String key = injectionPoint.key.value();
            String propertyTypeClassName = classNameOf(injectionPoint.element).toString();
            logger.info(() -> "Found property " + key + " of type " + propertyTypeClassName + " for " + typeElement);

            propertyContext.addProperty(key, item -> item.withDescription(getDescription(injectionPoint.element))
                    .withType(propertyTypeClassName)
                    .withSourceType(typeElement.asType().toString())
                    .withDefaultValue(injectionPoint.defaultValue)
                    .withDeprecation(deprecation(injectionPoint.element))
            );
        }

        for (FieldWithSetter fieldWithSetter : allFieldsWithSetters) {
            String key = fieldWithSetter.determinePropertyKey().value();
            String propertyTypeClassName = classNameOf(fieldWithSetter.field).toString();
            logger.info(() -> "Found property " + key + " of type " + propertyTypeClassName + " for " + typeElement);

            propertyContext.addProperty(key, item -> item
                    .withDescription(firstNotNull(getDescription(fieldWithSetter.field), getDescription(fieldWithSetter.setter)))
                    .withType(propertyTypeClassName)
                    .withSourceType(typeElement.asType().toString())
                    .withDefaultValue(fieldWithSetter.determineDefaultValue())
                    .withSourceMethod(fieldWithSetter.setter.getSimpleName().toString())
                    .withDeprecation(firstNotNull(deprecation(fieldWithSetter.field), deprecation(fieldWithSetter.setter)))
            );
        }

        if (allFieldsWithSetters.isEmpty()) {
            return builder.add("new $T(\n", typeElement.asType())
                    .indent()
                    .add("$L\n", constructorParameters(injectionPoints))
                    .unindent()
                    .add(")")
                    .build();
        } else {
            builder.add("$T.of(\n", DynamicBuilder.class)
                    .indent()
                    .add("new $T(\n", typeElement.asType())
                    .indent()
                    .add("$L\n", constructorParameters(injectionPoints))
                    .unindent()
                    .add(")\n")
                    .unindent()
                    .add(").setup(instance -> {\n")
                    .indent();

            allFieldsWithSetters.forEach(fieldWithSetter -> {
                // TODO: Support setters in nested objects
                Key key = fieldWithSetter.determinePropertyKey();
                if (isCollection(fieldWithSetter.field.asType())) {
                    builder.add("instance.$L($T.just($L));\n", fieldWithSetter.setter.getSimpleName(), Key.class, constructAll(key, fieldWithSetter.field()));
                } else if (fieldWithSetter.determineDefaultValue() == null) {
                    builder.add("properties.get($T.just($S), $T.class).ifPresent(instance::$L);\n", Key.class, key, classNameOf(fieldWithSetter.field), fieldWithSetter.setter.getSimpleName());
                } else {
                    builder.add("instance.$L($L);\n", fieldWithSetter.setter.getSimpleName(), getProperty(key, fieldWithSetter.determineDefaultValue(), fieldWithSetter.field(), !fieldWithSetter.hasAnnotation("Nullable")));
                }
            });
            builder.unindent().add("})");

            return builder.build();
        }
    }

    private CodeBlock compileTime(
            ClassEntity<?> entity,
            String prefix,
            String file
    ) {
        List<PropertyInjectionPoint> injectionPoints = constructorInjectionPoints((TypeElement) types().asElement(entity.rootType()), prefix);
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

    private CodeBlock constructorParameters(List<PropertyInjectionPoint> injectionPoints) {
        return CodeBlock.join(
                injectionPoints.stream()
                        .map(point -> getProperty(
                                        point.key(),
                                        point.defaultValue(),
                                        point.element(),
                                        point.required
                                )
                        )
                        .toList(),
                "," + System.lineSeparator()
        );
    }

    private CodeBlock getProperty(Key key, String defaultValue, Element element, boolean require) {
        if (defaultValue == null || defaultValue.isBlank()) {
            return getPropertyWithoutDefaultValue(key, element, require);
        } else {
            return getPropertyWithDefaultValue(key, defaultValue, element);
        }
    }

    private CodeBlock getPropertyWithoutDefaultValue(Key key, Element element, boolean require) {
        if (types().isAssignable(element.asType(), stringType)) {
            if (require) {
                logger.debug(() -> "Requiring " + key + " of type " + element.asType());
                return CodeBlock.of("properties.require($T.just($S))", Key.class, key.value());
            } else {
                return CodeBlock.of("properties.get($T.just($S)).orElse(null)", Key.class, key.value());
            }
        } else if (isCollection(element.asType())) {
            return constructAll(key, element);
        } else if (isNestedClass(element)) {
            return runtimeConstructorInvocation((TypeElement) types().asElement(element.asType()), key.value());
        } else {
            if (require) {
                return CodeBlock.of("properties.require($T.just($S), $T.class)", Key.class, key.value(), classNameOf(element));
            } else {
                return CodeBlock.of("properties.get($T.just($S), $T.class).orElse(null)", Key.class, key.value(), classNameOf(element));
            }
        }
    }

    private CodeBlock getPropertyWithDefaultValue(Key key, String defaultValue, Element element) {
        if (types().isAssignable(element.asType(), stringType)) {
            return CodeBlock.of("properties.get($T.just($S), $S)", Key.class, key.value(), defaultValue);
        } else if (isCollection(element.asType())) {
            return constructAll(key, element);
        } else if (isNestedClass(element)) {
            return runtimeConstructorInvocation((TypeElement) types().asElement(element.asType()), key.value());
        } else {
            return CodeBlock.of("properties.get($T.just($S), $T.class, $S)", Key.class, key.value(), classNameOf(element), defaultValue);
        }
    }

    private boolean isNestedClass(Element element) {
        if (element.getEnclosingElement().getKind() == ElementKind.CLASS) {
            org.slf4j.LoggerFactory.getLogger(CreateInstanceForPropertyBindingMethod.class).info("The class " + element + " is a subclass of " + element.getEnclosingElement());
            return true;
        }

        return false;
    }

    private CodeBlock constructAll(Key key, Element element) {
        DeclaredType declaredType = (DeclaredType) element.asType();
        TypeMirror typeMirror = declaredType.getTypeArguments().getFirst();

        if (types().isAssignable(element.asType(), stringType)) {
            return CodeBlock.of("properties.getAll($T.just($S))", Key.class, key.value());
        } else if (typeMirror.getKind().isPrimitive() || isEnum(typeMirror)) {
            return CodeBlock.of("properties.getAll($T.just($S), $T.class)", Key.class, key.value(), classNameOf(typeMirror));
        } else {
            throw new ProcessingException(element, "Cannot construct a list of properties for this type");
        }
    }

    private boolean isEnum(TypeMirror typeMirror) {
        if (typeMirror.getKind() == TypeKind.DECLARED) {
            TypeElement typeElement = (TypeElement) ((DeclaredType) typeMirror).asElement();
            return typeElement.getKind() == ElementKind.ENUM;
        }
        return false;
    }

    private boolean isCollection(TypeMirror typeMirror) {
        return types().isAssignable(types().erasure(typeMirror), collectionType);
    }

    private List<PropertyInjectionPoint> constructorInjectionPoints(TypeElement typeElement, String prefix) {
        return TypeUtils.findPrimaryConstructor(typeElement)
                .map(it -> findAllIn(prefix, it))
                .orElse(Collections.emptyList());
    }

    private List<PropertyInjectionPoint> findAllIn(String prefix, ExecutableElement constructor) {
        List<? extends VariableElement> elements = constructor.getParameters();
        return elements.stream()
                .map(element -> new PropertyInjectionPoint(
                                propertyName(element).withPrefix(prefix),
                                defaultValue(element),
                                element,
                                !Annotations.hasByName(element, "Nullable")
                        )
                ).toList();
    }

    private List<FieldWithSetter> findAllFieldsWithSetters(TypeElement typeElement) {
        List<FieldWithSetter> result = new ArrayList<>();
        Map<String, ExecutableElement> methods = typeElement.getEnclosedElements()
                .stream()
                .filter(it -> it.getKind() == ElementKind.METHOD)
                .map(it -> (ExecutableElement) it)
                .filter(it -> it.getParameters().size() == 1)
                .filter(it -> it.getSimpleName().toString().startsWith("set"))
                .collect(Collectors.toMap(it -> it.getSimpleName().toString(), it -> it));

        typeElement.getEnclosedElements().stream()
                .filter(it -> it.getKind() == ElementKind.FIELD)
                .map(it -> (VariableElement) it)
                .forEach(field -> {
                    String fieldName = field.getSimpleName().toString();
                    String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    Optional.ofNullable(methods.get(setterName))
                            .ifPresent(method -> {
                                if (isSetterOf(field, method)) {
                                    result.add(new FieldWithSetter(field, method));
                                }
                            });
                });

        return result;
    }

    public boolean isSetterOf(VariableElement field, ExecutableElement setter) {
        // TODO: Check for final fields and support values
        return setter.getParameters().size() == 1
                && setter.getParameters().getFirst().asType().equals(field.asType());
    }

    record PropertyInjectionPoint(
            @NotNull Key key,
            @Nullable String defaultValue,
            @NotNull VariableElement element,
            boolean required
    ) {
    }

    record FieldWithSetter(
            @NotNull VariableElement field,
            @NotNull ExecutableElement setter
    ) {
        public Key determinePropertyKey() {
            return tryFindPropertyName(setter)
                    .or(() -> tryFindPropertyName(setter.getParameters().getFirst()))
                    .orElseGet(() -> propertyName(field));
        }

        @Nullable
        public String determineDefaultValue() {
            return tryFindDefaultValue(setter)
                    .or(() -> tryFindDefaultValue(setter.getParameters().getFirst()))
                    .orElseGet(() -> defaultValue(field));
        }

        public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
            return Annotations.isAnnotatedWith(field, annotationType)
                    || Annotations.isAnnotatedWith(setter, annotationType);
        }

        public boolean hasAnnotation(String annotationType) {
            logger.info(() -> "Checking for instance " + annotationType + " on " + field + " and " + setter);
            return Annotations.hasByName(field, annotationType)
                    || Annotations.hasByName(setter, annotationType);
        }

        public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationType) {
            return Annotations.getAnnotation(setter, annotationType)
                    .or(() -> Annotations.getAnnotation(field, annotationType));
        }
    }
}
