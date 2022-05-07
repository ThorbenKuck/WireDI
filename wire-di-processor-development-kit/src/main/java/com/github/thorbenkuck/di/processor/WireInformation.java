package com.github.thorbenkuck.di.processor;

import com.squareup.javapoet.ClassName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.*;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WireInformation {

    /**
     * The root class, which contains the annotation
     */
    private final TypeElement annotationRootType;
    private final PackageElement targetPackage;
    @Nullable
    private final ExecutableElement primaryConstructor;
    @Nullable
    private final Integer wirePriority;
    private final boolean singleton;
    private final TypeElement primaryWireType;
    private boolean forceSingleton = false;
    private final boolean proxyExpected;
    private final List<TypeElement> wiredToElements;
    private final ExecutableElement builderMethod;

    public WireInformation(
            @NotNull TypeElement annotationRootType,
            @NotNull TypeElement primaryWireType,
            @NotNull PackageElement targetPackage,
            @Nullable ExecutableElement primaryConstructor,
            boolean singleton,
            boolean proxyExpected,
            @Nullable Integer wirePriority,
            @NotNull List<TypeElement> wiredToElements,
            @Nullable ExecutableElement builderMethod
    ) {
        this.annotationRootType = Objects.requireNonNull(annotationRootType, "The annotation root is required");
        this.targetPackage = targetPackage;
        this.primaryConstructor = primaryConstructor;
        this.wirePriority = wirePriority;
        this.singleton = singleton;
        this.primaryWireType = primaryWireType;
        this.proxyExpected = proxyExpected;
        this.wiredToElements = wiredToElements;
        this.builderMethod = builderMethod;
    }

    public static Builder builderFor(TypeElement typeElement) {
        return new Builder(typeElement);
    }

    public <T extends Annotation> Optional<T> findAnnotationOnRoot(Class<T> annotationType) {
        Optional<T> primaryOptional = Optional.ofNullable(
                primaryWireType.getAnnotation(annotationType)
        );

        if(primaryOptional.isPresent()) {
            return primaryOptional;
        }

        return Optional.ofNullable(
                annotationRootType.getAnnotation(annotationType)
        );
    }

    public String simpleClassName() {
        if(getBuilderMethod().isPresent()) {
            return getBuilderMethod()
                    .map(it -> it.getSimpleName().toString())
                    .map(it -> it.substring(0, 1).toUpperCase() + it.substring(1))
                    .map(it -> primaryWireType.getSimpleName().toString() + "Provider$" + it)
                    .orElseThrow(IllegalStateException::new);
        } else {
            return primaryWireType.getSimpleName().toString();
        }
    }

    public TypeElement getSuggestedRoot() {
        return primaryWireType;
    }

    public Optional<ExecutableElement> getPrimaryConstructor() {
        return Optional.ofNullable(primaryConstructor);
    }

    public Optional<Integer> getWirePriority() {
        return Optional.ofNullable(wirePriority);
    }

    public boolean isSingleton() {
        return forceSingleton || singleton;
    }

    public TypeElement getPrimaryWireType() {
        return primaryWireType;
    }

    public void forceSingleton() {
        this.forceSingleton = true;
    }

    public PackageElement getTargetPackage() {
        return targetPackage;
    }

    public List<TypeElement> getAllWireCandidates() {
        return wiredToElements;
    }

    public ClassName primaryClassName() {
        return ClassName.get(primaryWireType);
    }

    public ClassName realClassName() {
        return ClassName.get(annotationRootType);
    }

    public TypeElement getAnnotationRootType() {
        return annotationRootType;
    }

    public boolean isProxyExpected() {
        return proxyExpected;
    }

    public Optional<ExecutableElement> getBuilderMethod() {
        return Optional.ofNullable(builderMethod);
    }

    public static class Builder {
        final TypeElement annotationRootType;
        private PackageElement targetPackage;
        @Nullable
        private ExecutableElement primaryConstructor;
        @Nullable
        private Integer wirePriority;
        private boolean singleton = true;
        private TypeElement primaryWireType;
        private boolean proxyExpected = false;
        private final List<TypeElement> wiredToElements = new ArrayList<>();
        @Nullable
        private ExecutableElement builderMethod;

        private Builder(@NotNull TypeElement around) {
            this.annotationRootType = Objects.requireNonNull(around);
        }

        public WireInformation build() {
            if(targetPackage == null) {
                autoDeterminePackage();
            }
            return new WireInformation(
                    annotationRootType,
                    Objects.requireNonNull(primaryWireType, "No PrimaryWireType set"),
                    targetPackage,
                    primaryConstructor,
                    singleton,
                    proxyExpected,
                    wirePriority,
                    wiredToElements,
                    builderMethod
            );
        }

        public Builder autoDeterminePackage() {
            PackageElement packageElement = null;
            Element currentRoot = annotationRootType;

            while(packageElement == null) {
                currentRoot = currentRoot.getEnclosingElement();
                if(currentRoot instanceof PackageElement) {
                    packageElement = (PackageElement) currentRoot;
                }
            }

            targetPackage = Objects.requireNonNull(packageElement, "[TECHNICAL] Something is wrong...");

            return this;
        }

        public Builder atPackage(PackageElement targetPackage) {
            this.targetPackage = targetPackage;

            return this;
        }

        public Builder withPrimaryConstructor(ExecutableElement primaryConstructor) {
            this.primaryConstructor = primaryConstructor;

            return this;
        }

        public Builder withWirePriority(Integer wirePriority) {
            this.wirePriority = wirePriority;

            return this;
        }

        public Builder asSingleton(boolean singleton) {
            this.singleton = singleton;

            return this;
        }

        public Builder withPrimaryWireType(TypeElement primaryWireType) {
            this.primaryWireType = primaryWireType;

            return this;
        }

        public Builder enableProxy() {
            this.proxyExpected = true;

            return this;
        }

        public Builder asProxy(boolean proxyExpected) {
            this.proxyExpected = proxyExpected;

            return this;
        }

        public Builder addWiredToElements(List<TypeElement> wiredToElements) {
            this.wiredToElements.addAll(wiredToElements);

            return this;
        }

        public Builder addWiredToElement(TypeElement wiredToElement) {
            this.wiredToElements.add(wiredToElement);

            return this;
        }

        public Builder setWiredToElements(List<TypeElement> wiredToElements) {
            this.wiredToElements.clear();
            this.wiredToElements.addAll(wiredToElements);

            return this;
        }

        public Builder setWiredToElement(TypeElement wiredToElement) {
            this.wiredToElements.clear();
            this.wiredToElements.add(wiredToElement);

            return this;
        }

        public Builder buildByMethod(ExecutableElement method) {
            this.builderMethod = method;

            return this;
        }
    }
}
