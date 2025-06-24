package com.wiredi.compiler.processor.plugins;

import com.google.auto.service.AutoService;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.entities.IdentifiableProviderEntity;
import com.wiredi.compiler.logger.slf4j.CompileTimeLogger;
import com.wiredi.compiler.logger.slf4j.CompileTimeLoggerFactory;
import com.wiredi.runtime.domain.conditional.Conditional;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@AutoService(CompilerEntityPlugin.class)
public class ConditionalProcessorPlugin implements CompilerEntityPlugin {

    private static final CompileTimeLogger logger = CompileTimeLoggerFactory.getLogger(ConditionalProcessorPlugin.class);

    @Inject
    private Elements elements;

    @Override
    public void handle(@NotNull IdentifiableProviderEntity entity) {
        List<ConditionEntry> conditionalAnnotations = entity.findAnnotations(Conditional.class)
                .stream()
                .map(it -> {
                    TypeMirror conditionalEvaluatorType = Annotations.extractType(it.instance(), Conditional::value);
                    return new ConditionEntry(it.metadata(), conditionalEvaluatorType, elements.getTypeElement(conditionalEvaluatorType.toString()));
                }).toList();

        if (conditionalAnnotations.isEmpty()) {
            return;
        }

        logger.debug(() -> "Conditional detected for: " + entity);
        if (conditionalAnnotations.size() == 1) {
            ConditionEntry first = conditionalAnnotations.getFirst();
            entity.addMethod(new SingleConditionMethodFactory(first));
        } else {
            entity.addMethod(new BatchConditionMethodFactory(conditionalAnnotations));
        }
    }
}
