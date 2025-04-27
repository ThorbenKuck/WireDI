package com.wiredi.runtime.aspects;

import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;

public class RootMethod implements AspectHandler {

    @NotNull
    private final AspectHandler rootMethodAspectHandler;
    @NotNull
    private final String methodName;
    @NotNull
    private final Map<@NotNull String, @NotNull TypeIdentifier<?>> parameterTypes;
    @NotNull
    private final List<@NotNull AnnotationMetaData> annotations;

    public RootMethod(
            @NotNull AspectHandler rootMethodAspectHandler,
            @NotNull String methodName,
            @NotNull Map<String, TypeIdentifier<?>> parameterTypes,
            @NotNull List<@NotNull AnnotationMetaData> annotations
    ) {
        this.rootMethodAspectHandler = rootMethodAspectHandler;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.annotations = annotations;
    }

    public static RootMethod just(
            @NotNull String methodName,
            @NotNull AspectHandler aspectHandler
    ) {
        return new Builder(methodName).build(aspectHandler);
    }

    public static Builder builder(String methodName) {
        return new Builder(methodName);
    }

    @NotNull
    public String getMethodName() {
        return methodName;
    }

    @NotNull
    public List<AnnotationMetaData> getAnnotations() {
        return annotations;
    }

    @NotNull
    public Map<String, TypeIdentifier<?>> parameterTypes() {
        return parameterTypes;
    }

    public Optional<AnnotationMetaData> findAnnotation(Predicate<AnnotationMetaData> predicate) {
        for (AnnotationMetaData annotationMetaData : annotations) {
            if (predicate.test(annotationMetaData)) {
                return Optional.of(annotationMetaData);
            }
        }

        return Optional.empty();
    }

    public Optional<AnnotationMetaData> findAnnotation(Class<? extends Annotation> annotation) {
        for (AnnotationMetaData annotationMetaData : annotations) {
            if (Objects.equals(annotationMetaData.className(), annotation.getName())) {
                return Optional.of(annotationMetaData);
            }
        }

        return Optional.empty();
    }

    public AnnotationMetaData getAnnotation(Class<? extends Annotation> annotation) {
        for (AnnotationMetaData annotationMetaData : annotations) {
            if (Objects.equals(annotationMetaData.className(), annotation.getName())) {
                return annotationMetaData;
            }
        }

        throw new IllegalArgumentException("The method " + methodName + " is not annotated with " + annotation.getName());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof RootMethod that)) return false;
        return Objects.equals(rootMethodAspectHandler, that.rootMethodAspectHandler)
                && Objects.equals(methodName, that.methodName)
                && Objects.equals(parameterTypes, that.parameterTypes)
                && Objects.equals(annotations, that.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootMethodAspectHandler, methodName, parameterTypes, annotations);
    }

    @Override
    public String toString() {
        return "RootMethod{" +
                "rootMethod=" + rootMethodAspectHandler +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" + parameterTypes +
                ", annotations=" + annotations +
                '}';
    }

    @Override
    public @Nullable Object process(@NotNull ExecutionContext context) {
        return rootMethodAspectHandler.process(context);
    }

    public static class Builder {

        @NotNull
        private final String methodName;
        @NotNull
        private final Map<@NotNull String, @NotNull TypeIdentifier<?>> parameters = new HashMap<>();
        @NotNull
        private final List<@NotNull AnnotationMetaData> annotations = new ArrayList<>();

        public Builder(@NotNull String methodName) {
            this.methodName = methodName;
        }

        public Builder withParameter(
                @NotNull String name,
                @NotNull TypeIdentifier<?> type
        ) {
            parameters.put(name, type);
            return this;
        }

        public Builder withAnnotation(@NotNull AnnotationMetaData annotation) {
            this.annotations.add(annotation);
            return this;
        }

        public RootMethod build(@NotNull AspectHandler rootMethod) {
            return new RootMethod(rootMethod, methodName, parameters, annotations);
        }
    }
}
