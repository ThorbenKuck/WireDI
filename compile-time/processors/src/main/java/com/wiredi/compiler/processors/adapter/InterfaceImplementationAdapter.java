package com.wiredi.compiler.processors.adapter;

import com.wiredi.annotations.Wire;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.processor.plugins.CompilerEntityPlugin;
import com.wiredi.compiler.processor.plugins.ProcessorPluginContext;
import com.wiredi.compiler.repository.CompilerRepository;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.TypeElement;

public class InterfaceImplementationAdapter {

    @Inject
    private ProcessorPluginContext pluginContext;

    @Inject
    private CompilerRepository compilerRepository;

    private final Logger logger = Logger.get(InterfaceImplementationAdapter.class);

    public void handle(@NotNull TypeElement typeElement, @Nullable Wire annotation) {
        for (CompilerEntityPlugin wireProcessorPlugin : pluginContext.wireProcessorPlugins) {
            ClassEntity implementation = wireProcessorPlugin.implementInterface(typeElement, annotation);
            if (implementation != null) {
                logger.info(typeElement, () -> "Implemented using the plugin: " + wireProcessorPlugin.getClass().getName());
                compilerRepository.save(implementation);
                return;
            }
        }

        logger.error(() -> "No interface implementor found for " + typeElement + ". Please make sure to provide an adequate ProcessorPlugin that can implement this interface.");
    }
}
