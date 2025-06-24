package com.wiredi.compiler.processor.plugins;

import com.google.auto.service.AutoService;
import com.wiredi.compiler.domain.Qualifiers;
import com.wiredi.compiler.domain.entities.IdentifiableProviderEntity;
import com.wiredi.compiler.logger.slf4j.CompileTimeLogger;
import com.wiredi.compiler.logger.slf4j.CompileTimeLoggerFactory;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AutoService(CompilerEntityPlugin.class)
public class QualifierProcessorPlugin implements CompilerEntityPlugin {

    private static final CompileTimeLogger logger = CompileTimeLoggerFactory.getLogger(QualifierProcessorPlugin.class);

    @Override
    public void handle(@NotNull IdentifiableProviderEntity entity) {
        logger.debug(() -> "Handling qualifiers in " + entity);
        doHandleOn(entity);
        entity.onChildren(this::doHandleOn);
    }

    private void doHandleOn(IdentifiableProviderEntity entity) {
        List<QualifierType> qualifiers = Qualifiers.allQualifiersOf(entity.getSource());

        if (!qualifiers.isEmpty()) {
            logger.debug(() -> "The entity " + entity.className() + " defined by " + entity.getSource() + " has the qualifiers " + qualifiers);
            entity.addMethod(new QualifiersMethod(qualifiers));
        }
    }
}
