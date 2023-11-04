package com.wiredi.processor.plugins;

import com.google.auto.service.AutoService;
import com.wiredi.compiler.domain.Qualifiers;
import com.wiredi.compiler.domain.entities.IdentifiableProviderEntity;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.qualifier.QualifierType;

import java.util.List;

@AutoService(CompilerEntityPlugin.class)
public class QualifierProcessorPlugin implements CompilerEntityPlugin {

    private static final Logger logger = Logger.get(QualifierProcessorPlugin.class);

    @Override
    public void initialize() {
        logger.info(() -> "QualifierHere: Initialize");
    }

    @Override
    public void handle(IdentifiableProviderEntity entity) {
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
