package com.github.thorbenkuck.di.processor.processors;

import com.github.thorbenkuck.di.annotations.aspects.Aspect;
import com.github.thorbenkuck.di.annotations.aspects.Facet;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.builder.AspectInstanceClassBuilder;
import com.github.thorbenkuck.di.processor.builder.AspectFactoryClassBuilder;
import com.github.thorbenkuck.di.processor.builder.IdentifiableProviderClassBuilder;
import com.github.thorbenkuck.di.processor.constructors.MethodConstructor;
import com.github.thorbenkuck.di.processor.DiProcessor;
import com.github.thorbenkuck.di.processor.exceptions.ProcessingException;
import com.github.thorbenkuck.di.processor.WireAnnotationInformationExtractor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class FacetProcessor extends DiProcessor {
    @Override
    protected Class<? extends Annotation> targetAnnotation() {
        return Facet.class;
    }

    @Override
    protected void handle(Element element) {
        if (!element.getKind().isClass()) {
            throw new ProcessingException(element, "Sorry, but only classes may be annotated with @Facet");
        }

        if (element.getModifiers().contains(Modifier.FINAL)) {
            throw new ProcessingException(element, "Facet classes are proxied and therefor may not be final. We want to do as little reflections as possible." + System.lineSeparator()
                    + "Please remove the final modifier");
        }
        TypeElement typeElement = (TypeElement) element;

        findAspectAnnotatedMethods(typeElement).forEach(method -> {
            Aspect aspectAnnotation = method.getAnnotation(Aspect.class);
            TypeMirror annotationType;
            try {
                aspectAnnotation.around();
                throw new ProcessingException(method, "There is a technical issue. We could not extract \"around\" type");
            } catch (javax.lang.model.type.MirroredTypesException e) {
                List<? extends TypeMirror> typeMirrors = e.getTypeMirrors();
                annotationType = typeMirrors.get(0);
            }

            TypeSpec aspectInstanceClass = new AspectInstanceClassBuilder(annotationType, typeElement, method)
                    .addConstructor()
                    .addDelegateField()
                    .addProcessMethod()
                    .build();

            new AspectFactoryClassBuilder(aspectInstanceClass, annotationType, typeElement)
                    .addBuildMethod()
                    .addAroundAnnotationMethod()
                    .appendAspectInstanceClass()
                    .markAsGenerated("Creates the aspect Provider")
                    .writeClass();
        });

        WireInformation wireInformation = WireAnnotationInformationExtractor.extractOf(typeElement);
        wireInformation.forceSingleton();

        new IdentifiableProviderClassBuilder(wireInformation)
                .overwriteAllRequiredMethods()
                .applyMethodBuilder(MethodConstructor.createInstanceForWire())
                .buildAndWrite("This class is used to identify the Facet");
    }

    private List<? extends ExecutableElement> findAspectAnnotatedMethods(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .filter(it -> it.getAnnotation(Aspect.class) != null)
                .filter(it -> it instanceof ExecutableElement)
                .map(it -> (ExecutableElement) it)
                .collect(Collectors.toList());
    }
}
