package com.wiredi.compiler.processors;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.wiredi.annotations.ActiveProfiles;
import com.wiredi.annotations.Order;
import com.wiredi.compiler.domain.AnnotationField;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.entities.EnvironmentConfigurationEntity;
import com.wiredi.compiler.domain.entities.methods.identifiableprovider.OrderMethod;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.runtime.domain.Ordered;
import com.wiredi.runtime.environment.builtin.ProfilePropertiesEnvironmentConfiguration;
import com.wiredi.compiler.processor.lang.processors.WireBaseProcessor;
import jakarta.inject.Inject;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import static com.wiredi.runtime.environment.DefaultEnvironmentKeys.ACTIVE_PROFILES;

@AutoService(Processor.class)
public class ActiveProfilesWireProcessor extends WireBaseProcessor {

    private static final Logger logger = Logger.get(ActiveProfilesWireProcessor.class);

    @Inject
    private CompilerRepository compilerRepository;

    @Inject
    private Annotations annotations;

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

        logger.info("Setting active profiles to " + activeProfile);
        compilerRepository.newEnvironmentConfiguration(typeElement)
                .addMethod(new OrderMethod(CodeBlock.of("$T.before($T.ORDER)", Ordered.class, ProfilePropertiesEnvironmentConfiguration.class)))
                .addAnnotation(AnnotationSpec.builder(Order.class)
                        .addMember("before", "$T.class", ProfilePropertiesEnvironmentConfiguration.class)
                        .build())
                .appendEntry(new EnvironmentConfigurationEntity.Entry(ACTIVE_PROFILES, String.join(",", activeProfile)));
    }
}
