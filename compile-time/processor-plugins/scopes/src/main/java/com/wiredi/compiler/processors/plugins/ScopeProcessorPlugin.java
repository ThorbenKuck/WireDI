package com.wiredi.compiler.processors.plugins;

import com.google.auto.service.AutoService;
import com.wiredi.compiler.domain.Scopes;
import com.wiredi.compiler.domain.entities.IdentifiableProviderEntity;
import com.wiredi.compiler.logger.slf4j.CompileTimeLogger;
import com.wiredi.compiler.logger.slf4j.CompileTimeLoggerFactory;
import com.wiredi.compiler.processor.plugins.CompilerEntityPlugin;
import com.wiredi.runtime.scope.ScopeType;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.util.Elements;
import java.util.List;

@AutoService(CompilerEntityPlugin.class)
public class ScopeProcessorPlugin implements CompilerEntityPlugin {

    private static final CompileTimeLogger logger = CompileTimeLoggerFactory.getLogger(ScopeProcessorPlugin.class);

    @Inject
    private Scopes scopes;

    @Inject
    private Elements elements;

    @Override
    public void handle(@NotNull IdentifiableProviderEntity entity) {
        logger.info(() -> "Handling scopes in " + entity);
        doHandleOn(entity);
        entity.onChildren(this::doHandleOn);
    }

    private void doHandleOn(IdentifiableProviderEntity entity) {
        List<ScopeType> scopes = this.scopes.allScopesOf(entity.getSource());
        if (!scopes.isEmpty()) {
            logger.info(() -> "The entity " + entity.className() + " defined by " + entity.getSource() + " has the scopes " + scopes);
            entity.addMethod(new ScopeMethod(scopes, elements));
        }
    }
}
