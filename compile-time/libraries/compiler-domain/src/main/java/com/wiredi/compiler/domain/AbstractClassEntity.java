package com.wiredi.compiler.domain;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.entities.FieldFactory;
import com.wiredi.compiler.domain.entities.methods.MethodFactory;
import com.wiredi.compiler.logger.Logger;
import jakarta.annotation.Generated;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractClassEntity<T extends ClassEntity<T>> implements ClassEntity<T> {

    @NotNull
    protected final TypeSpec.Builder builder;
    @NotNull
    private final String className;
    @NotNull
    private final TypeMirror rootElement;
    @NotNull
    private final Element source;
    @NotNull
    private final Map<String, MethodSpec> methods = new HashMap<>();
    @NotNull
    private final Map<String, FieldSpec> fields = new HashMap<>();
    @Nullable
    private MethodSpec constructor;
    @Nullable
    private PackageElement packageElement;
    private boolean valid = true;

    public AbstractClassEntity(
            @NotNull Element source,
            @NotNull TypeMirror rootElement,
            @NotNull String className
    ) {
        this.rootElement = rootElement;
        this.source = source;
        this.className = className;
        this.builder = createBuilder(rootElement)
                .addAnnotation(generatedAnnotation())
                .addOriginatingElement(source);

        List<Class<?>> autoServiceType = autoServiceTypes();
        if (!autoServiceType.isEmpty()) {
            builder.addAnnotation(autoServiceAnnotation(autoServiceType));
        }
    }

    @Override
    public <A extends Annotation> List<Annotations.Result<A>> findAnnotations(Class<A> type) {
        List<Annotations.Result<A>> annotations = Annotations.findAll(type, source);
        if (source instanceof ExecutableElement) {
            Element element = source;
            while(!(element instanceof TypeElement)) {
                element = element.getEnclosingElement();
            }
            annotations.addAll(Annotations.findAll(type, element));
        }
        return annotations;
    }

    @Override
    public void invalidate() {
        this.valid = false;
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }

    @NotNull
    public Element getSource() {
        return source;
    }

    @Override
    public T setConstructor(MethodFactory methodFactory) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder();
        methodFactory.append(builder, this);
        constructor = builder.build();

        return (T) this;
    }

    @Override
    public T addMethod(String name, MethodFactory methodFactory) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(name);
        methodFactory.append(builder, this);
        methods.put(name, builder.build());

        return (T) this;
    }

    @Override
    public T addField(TypeName type, String name, FieldFactory fieldFactory) {
        FieldSpec.Builder builder = FieldSpec.builder(type, name);
        fieldFactory.append(builder, this);
        fields.put(name, builder.build());

        return (T) this;
    }

    @Override
    public T addAnnotation(AnnotationSpec annotationSpec) {
        this.builder.addAnnotation(annotationSpec);
        return (T) this;
    }

    @Override
    public T addInterface(TypeName typeName) {
        this.builder.addSuperinterface(typeName);
        return (T) this;
    }

    @Override
    public Optional<PackageElement> packageElement() {
        return Optional.ofNullable(this.packageElement);
    }

    private final Logger logger = Logger.get(getClass());

    @Override
    public T setPackage(PackageElement packageElement) {
        addSource(packageElement);
        this.packageElement = packageElement;
        return (T) this;
    }

    @Override
    public T addSource(Element element) {
        builder.addOriginatingElement(element);
        return (T) this;
    }

    @Override
    public ClassName compileFinalClassName() {
        return packageElement()
                .map(p -> ClassName.get(p.getQualifiedName().toString(), className))
                .orElseThrow(() -> new IllegalStateException("Package not set for class " + className));
    }

    @Override
    public String className() {
        return className;
    }

    protected abstract TypeSpec.Builder createBuilder(TypeMirror type);

    protected void finalize(TypeSpec.Builder builder) {
    }

    private AnnotationSpec generatedAnnotation() {
        String generationTime = System.getProperty("wire-di.generation-time");
        OffsetDateTime offsetDateTime;
        if (generationTime != null) {
            offsetDateTime = OffsetDateTime.parse(generationTime);
        } else {
            offsetDateTime = OffsetDateTime.now();
        }

        AnnotationSpec.Builder generatedBuilder = AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", getClass().getName())
                .addMember("date", "$S", offsetDateTime.toString());

        String comments = comments();
        if (comments != null) {
            generatedBuilder.addMember("comments", "$S", comments);
        }

        return generatedBuilder.build();
    }

    private AnnotationSpec autoServiceAnnotation(List<Class<?>> types) {
        return AnnotationSpec.builder(AutoService.class)
                .addMember("value", "{$L}", CodeBlock.join(types.stream().map(type -> CodeBlock.builder().add("$T.class", type).build()).toList(), ", "))
                .build();
    }

    @Override
    public boolean willHaveTheSamePackageAs(Element element) {
        return packageElement()
                .map(p -> p.equals(packageElementOf(element)))
                .orElse(false);
    }

    public PackageElement packageElementOf(Element element) {
        Element current = element;
        while (!(current instanceof PackageElement)) {
            current = current.getEnclosingElement();
        }

        return (PackageElement) current;
    }

    @Override
    public final TypeSpec compile() {
        MethodSpec constructor = this.constructor;
        builder.addFields(fields.values());
        if (constructor != null) {
            builder.addMethod(constructor);
        }
        builder.addMethods(methods.values());

        finalize(builder);
        return builder.build();
    }

    @Override
    public final TypeMirror rootType() {
        return rootElement;
    }

    @Nullable
    public String comments() {
        return null;
    }

    @NotNull
    public List<Class<?>> autoServiceTypes() {
        return List.of();
    }

    @Override
    public boolean requiresReflectionFor(Element element) {
        if (element.getModifiers().contains(Modifier.PUBLIC)) {
            return false;
        }
        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            return true;
        }

        return packageElement().map(it -> !it.equals(packageElementOf(element))).orElse(true);
    }
}
