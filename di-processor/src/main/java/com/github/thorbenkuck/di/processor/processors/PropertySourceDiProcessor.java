package com.github.thorbenkuck.di.processor.processors;

import com.github.thorbenkuck.di.annotations.properties.PropertySource;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.builder.IdentifiableProviderClassBuilder;
import com.github.thorbenkuck.di.processor.constructors.MethodConstructor;
import com.github.thorbenkuck.di.processor.foundation.DiProcessor;
import com.github.thorbenkuck.di.processor.foundation.Logger;
import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

@AutoService(Processor.class)
public class PropertySourceDiProcessor extends DiProcessor {

    @Override
    protected Collection<Class<? extends Annotation>> supportedAnnotations() {
        return Collections.singletonList(PropertySource.class);
    }

    @Override
    protected void handle(Element element) {
        if (!isValidType(element)) {
            Logger.error("Only non-abstract classes are allowed to be annotated with @PropertySource", element);
            return;
        }

        if(element.getAnnotation(PropertySource.class) == null) {
            Logger.error("Meta annotations are currently not supported!", element);
            return;
        }

        TypeElement typeElement = (TypeElement) element;

        WireInformation wireInformation = WireInformation.extractOf(typeElement);
        wireInformation.forceSingleton();

        new IdentifiableProviderClassBuilder(wireInformation)
                .overwriteAllRequiredMethods()
                .applyMethodBuilder(MethodConstructor.createInstanceForPropertySource())
                .buildAndWrite("This class is used to identify wire capable properties");
    }

    private boolean isValidType(Element element) {
        return element.getKind().isClass()
                && !element.getModifiers().contains(Modifier.ABSTRACT);
    }
}
