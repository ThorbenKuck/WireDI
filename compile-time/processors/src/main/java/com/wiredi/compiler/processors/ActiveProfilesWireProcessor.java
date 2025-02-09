package com.wiredi.compiler.processors;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.wiredi.annotations.ActiveProfiles;
import com.wiredi.annotations.Order;
import com.wiredi.compiler.domain.AnnotationField;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.entities.EnvironmentConfigurationEntity;
import com.wiredi.compiler.domain.entities.environment.EnvironmentModification;
import com.wiredi.compiler.domain.entities.methods.identifiableprovider.OrderMethod;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.processor.lang.processors.WireBaseProcessor;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.runtime.environment.builtin.ProfilePropertiesEnvironmentConfiguration;
import jakarta.inject.Inject;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

@AutoService(Processor.class)
public class ActiveProfilesWireProcessor extends WireBaseProcessor {

    private static final Logger logger = Logger.get(ActiveProfilesWireProcessor.class);
    private static final OrderMethod DEFAULT_ORDER_METHOD = new OrderMethod(CodeBlock.of("$T.FIRST", Order.class));
    @Inject
    private CompilerRepository compilerRepository;
    @Inject
    private Annotations annotations;
    @Inject
    private Types types;

    @Override
    protected List<Class<? extends Annotation>> targetAnnotations() {
        return List.of(ActiveProfiles.class);
    }

    @Override
    protected void handle(Element element) {
        if (!element.getKind().isClass()) {
            logger.error(element, "The annotated element of PropertySource has to be a class!");
            return;
        }
        final TypeElement typeElement = (TypeElement) element;
        List<String> activeProfile = annotations.findAnnotationField(element, ActiveProfiles.class, "value")
                .map(AnnotationField::asArrayOfStrings)
                .orElse(Collections.emptyList());
        if (activeProfile.isEmpty()) {
            logger.warn(element, "No active profiles set!");
            return;
        }

        OrderMethod orderMethod = Annotations.getAnnotation(element, Order.class)
                .map(it -> new OrderMethod(it, types))
                .orElse(DEFAULT_ORDER_METHOD);

        logger.info("Setting active profiles to " + activeProfile);
        EnvironmentConfigurationEntity entity = compilerRepository.newEnvironmentConfiguration(typeElement)
                .addMethod(orderMethod)
                .addAnnotation(AnnotationSpec.builder(Order.class)
                        .addMember("before", "$T.class", ProfilePropertiesEnvironmentConfiguration.class)
                        .build());

        activeProfile.forEach(it -> entity.appendModification(EnvironmentModification.addActiveProfile(it)));
    }
}
