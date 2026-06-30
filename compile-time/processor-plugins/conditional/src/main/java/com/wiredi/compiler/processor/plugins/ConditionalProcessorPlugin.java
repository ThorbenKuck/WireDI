package com.wiredi.compiler.processor.plugins;

import com.google.auto.service.AutoService;
import com.wiredi.compiler.Injector;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.entities.IdentifiableProviderEntity;
import com.wiredi.compiler.logger.slf4j.CompileTimeLogger;
import com.wiredi.compiler.logger.slf4j.CompileTimeLoggerFactory;
import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.domain.conditional.Conditional;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnBean;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnBeanEvaluator;
import com.wiredi.runtime.values.Value;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@AutoService(CompilerEntityPlugin.class)
public class ConditionalProcessorPlugin implements CompilerEntityPlugin {

    private static final CompileTimeLogger logger = CompileTimeLoggerFactory.getLogger(ConditionalProcessorPlugin.class);

    @Inject
    private Annotations annotations;
    @Inject
    private Elements elements;
    private final Value<TypeElement> conditionalOnBeanElementValue = Value.empty();

    @Override
    public void handle(@NotNull IdentifiableProviderEntity entity) {
        logger.debug(() -> "Handling conditional in " + entity.getSource());
        List<ConditionEntry> conditionEntries = new ArrayList<>();
        TypeElement conditionalOnBeanTypeElement = conditionalOnBeanElementValue.getOrSet(() -> elements.getTypeElement(ConditionalOnBeanEvaluator.class.getName()));

        if (entity.getSource().getKind() == ElementKind.METHOD) {
            // Case: Provider method
            // The outer class must be present for the provider to be taken
            annotations.findAll(Conditional.class, entity.getSource()).stream()
                    .map(it -> {
                        TypeMirror conditionalEvaluatorType = Annotations.extractType(it.instance(), Conditional::value);
                        return new ConditionEntry(it.metadata(), conditionalEvaluatorType, elements.getTypeElement(conditionalEvaluatorType.toString()));
                    }).forEach(conditionEntries::add);
            conditionEntries.add(new ConditionEntry(
                            AnnotationMetadata.builder(ConditionalOnBean.class)
                                    .withField("type", entity.getSource().getEnclosingElement().asType())
                                    .build(),
                    conditionalOnBeanTypeElement.asType(),
                    conditionalOnBeanTypeElement
                    )
            );
        } else if (entity.getSource().getKind() == ElementKind.CLASS) {
            // Case: Class that is annotated with Conditional
            entity.findAnnotations(Conditional.class)
                    .stream()
                    .map(it -> {
                        TypeMirror conditionalEvaluatorType = Annotations.extractType(it.instance(), Conditional::value);
                        return new ConditionEntry(it.metadata(), conditionalEvaluatorType, elements.getTypeElement(conditionalEvaluatorType.toString()));
                    }).forEach(conditionEntries::add);
        } else {
            logger.warn(() -> "Unsupported IdentifiableProvider source " + entity.getSource() + ". This indicates that the ConditionalProcessorPlugin is not properly configured or outdated.");
            return;
        }


        if (conditionEntries.isEmpty()) {
            return;
        }

        logger.debug(() -> "Conditional detected for: " + entity);
        if (conditionEntries.size() == 1) {
            ConditionEntry first = conditionEntries.getFirst();
            entity.addMethod(new SingleConditionMethodFactory(first));
        } else {
            entity.addMethod(new BatchConditionMethodFactory(conditionEntries));
        }
    }
}
