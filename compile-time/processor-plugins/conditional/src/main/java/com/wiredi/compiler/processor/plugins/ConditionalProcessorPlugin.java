package com.wiredi.compiler.processor.plugins;

import com.google.auto.service.AutoService;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.entities.IdentifiableProviderEntity;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.runtime.domain.conditional.Conditional;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.type.TypeMirror;
import java.util.List;

@AutoService(CompilerEntityPlugin.class)
public class ConditionalProcessorPlugin implements CompilerEntityPlugin {

    private static final Logger logger = Logger.get(ConditionalProcessorPlugin.class);

    @Override
    public void handle(@NotNull IdentifiableProviderEntity entity) {
        List<ConditionEntry> conditionalAnnotations = entity.findAnnotations(Conditional.class)
                .stream()
                .map(it -> {
                    TypeMirror annotationType = Annotations.extractType(it.annotation(), Conditional::value);
                    return new ConditionEntry(it.annotationMetaData(), annotationType);
                }).toList();

        if (conditionalAnnotations.isEmpty()) {
            return;
        }

        logger.debug(() -> "Conditional detected for: " + entity);
        if (conditionalAnnotations.size() == 1) {
            ConditionEntry first = conditionalAnnotations.get(0);
            entity.addMethod(new SingleConditionMethodFactory(first));
        } else {
            entity.addMethod(new BatchConditionMethodFactory(conditionalAnnotations));
        }
    }
}
