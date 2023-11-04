package com.wiredi.processor.plugins;

import com.google.auto.service.AutoService;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.entities.IdentifiableProviderEntity;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.domain.conditional.Conditional;
import jakarta.inject.Inject;

import javax.lang.model.type.TypeMirror;
import java.util.List;

@AutoService(CompilerEntityPlugin.class)
public class ConditionalProcessorPlugin implements CompilerEntityPlugin {

    private static final Logger logger = Logger.get(ConditionalProcessorPlugin.class);

    @Inject
    private Annotations annotations;

    @Override
    public void handle(IdentifiableProviderEntity entity) {
        List<ConditionEntry> conditionalAnnotations = annotations.findAll(Conditional.class, entity)
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
