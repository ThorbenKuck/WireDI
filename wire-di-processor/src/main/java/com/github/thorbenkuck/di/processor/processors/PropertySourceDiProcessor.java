package com.github.thorbenkuck.di.processor.processors;

import com.github.thorbenkuck.di.annotations.properties.Properties;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.builder.IdentifiableProviderClassBuilder;
import com.github.thorbenkuck.di.processor.DiProcessor;
import com.github.thorbenkuck.di.processor.Logger;
import com.github.thorbenkuck.di.processor.WireAnnotationInformationExtractor;
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
    protected Class<? extends Annotation> targetAnnotation() {
        return Properties.class;
    }

    @Override
    protected void handle(Element element) {
        if (!isValidType(element)) {
            Logger.error(element, "Only non-abstract classes are allowed to be annotated with @PropertySource");
            return;
        }
        if(element.getAnnotation(Properties.class) == null) {
            Logger.error(element, "Meta annotations are currently not supported!");
            return;
        }

        TypeElement typeElement = (TypeElement) element;
        WireInformation wireInformation = WireAnnotationInformationExtractor.extractOf(typeElement);
        wireInformation.forceSingleton();

        new IdentifiableProviderClassBuilder(wireInformation)
                .overwriteAllRequiredMethods()
                .identifyingAPropertySource()
                .buildAndWrite("This class is used to identify wire capable properties");
    }

    private boolean isValidType(Element element) {
        return element.getKind().isClass()
                && !element.getModifiers().contains(Modifier.ABSTRACT);
    }
}
