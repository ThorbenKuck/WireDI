package com.github.thorbenkuck.di.processor.builder;

import com.github.thorbenkuck.di.annotations.PrimaryWireType;
import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.annotations.aspects.Pure;
import com.github.thorbenkuck.di.aspects.AspectRepository;
import com.github.thorbenkuck.di.domain.AspectAwareProxy;
import com.github.thorbenkuck.di.processor.Aop;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.ProcessorContext;
import com.squareup.javapoet.*;

import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AspectAwareProxyBuilder extends ClassBuilder {

    private final WireInformation wireInformation;

    public AspectAwareProxyBuilder(WireInformation information) {
        super(information);
        this.wireInformation = information;
        setup();
    }

    public static boolean willProxyAnything(WireInformation wireInformation) {
        if(wireInformation.getWireCandidate().getAnnotation(Pure.class) != null) {
            return false;
        }
        Optional<? extends AnnotationMirror> detectedAnnotations = AspectAwareProxyMethodOverwrite.findEligibleMethods(wireInformation)
                .stream()
                .flatMap(current -> Aop.getAopEnabledAnnotations(current).stream()).findAny();

        return detectedAnnotations.isPresent();
    }

    @Override
    protected TypeSpec.Builder initialize() {
        return TypeSpec.classBuilder(wireInformation.getWireCandidate().getSimpleName() + "$$AspectAwareProxy")
                .superclass(ClassName.get(wireInformation.getWireCandidate()))
                .addSuperinterface(ClassName.get(AspectAwareProxy.class))
                .addModifiers(Modifier.FINAL);
    }

    public AspectAwareProxyBuilder addDelegatingConstructors() {
        addField(
                FieldSpec.builder(ClassName.get(AspectRepository.class), "aspectRepository")
                        .addModifiers(Modifier.PRIVATE)
                        .addModifiers(Modifier.FINAL)
        );
        MethodSpec.Builder proxyConstructor = constructor();
        List<ParameterSpec> proxyConstructorParameters = new ArrayList<>();
        proxyConstructorParameters.add(ParameterSpec.builder(AspectRepository.class, "aspectRepository")
                .build());
        List<VariableElement> originalParameters = new ArrayList<>();

        if(wireInformation.getPrimaryConstructor().isPresent()) {
            ExecutableElement superConstructor = wireInformation.getPrimaryConstructor().get();
            proxyConstructorParameters.addAll(parametersOf(superConstructor));
            originalParameters.addAll(superConstructor.getParameters());
        }

        String superConstructorCall = originalParameters.stream()
                .map(VariableElement::getSimpleName)
                .collect(Collectors.joining(",", "super(", ")"));

        addMethod(
                proxyConstructor
                        .addParameters(proxyConstructorParameters)
                        .addStatement(superConstructorCall)
                        .addStatement("this.aspectRepository = aspectRepository")
        );

        return this;
    }

    private MethodSpec.Builder constructor() {
        return MethodSpec.constructorBuilder();
    }

    public AspectAwareProxyBuilder overwriteMethods() {
        new AspectAwareProxyMethodOverwrite(wireInformation).overwriteAllFor(classBuilder());

        return this;
    }

    public static boolean eligibleForProxy(TypeElement typeElement) {
        return !typeElement.getModifiers().contains(Modifier.FINAL);
    }

    public AspectAwareProxyBuilder appendWireAnnotations() {
        CodeBlock.Builder wireToBuilder = CodeBlock.builder()
                .add("{ ");
        List<ClassName> classNames = wireInformation.getAllWireCandidates()
                .stream()
                .map(ClassName::get)
                .collect(Collectors.toList());

        boolean first = true;
        for (ClassName className : classNames) {
            if(first) {
                first = false;
                wireToBuilder.add("$T.class", className);
            } else {
                wireToBuilder.add(", $T.class", className);
            }
        }

        addAnnotation(
                AnnotationSpec.builder(Wire.class)
                        .addMember("to", wireToBuilder.add(" }").build())
                        .addMember("singleton", "$L", wireInformation.isSingleton())
                        .addMember("proxy", "false")
        );

        addAnnotation(
                AnnotationSpec.builder(PrimaryWireType.class)
                        .addMember("value", "$T.class", ClassName.get(wireInformation.getPrimaryWireType()))
        );

        return this;
    }
}
