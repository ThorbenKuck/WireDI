package com.wiredi.maven.services;

import com.wiredi.maven.LoggerWrapper;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.List;

/**
 * Service for configuring the Maven Compiler plugin.
 */
public class CompilerConfigurationService {

    private final Xpp3DomService xpp3DomService;
    private final AnnotationProcessorConfigurationService annotationProcessorConfigurationService;
    private static final LoggerWrapper logger = LoggerWrapper.getInstance();

    public CompilerConfigurationService(Xpp3DomService xpp3DomService,
                                       AnnotationProcessorConfigurationService annotationProcessorConfigurationService) {
        this.xpp3DomService = xpp3DomService;
        this.annotationProcessorConfigurationService = annotationProcessorConfigurationService;
    }

    /**
     * Configures the compiler plugin with processor dependencies.
     *
     * @param plugin the Maven Compiler plugin
     * @param processorDependencies the processor dependencies to add
     */
    public void configureCompilerPlugin(Plugin plugin, List<Dependency> processorDependencies) {
        logger.debug("Found Maven Compiler Plugin - configuring annotation processors");

        Xpp3Dom config = xpp3DomService.getOrCreateConfiguration(plugin.getConfiguration());
        plugin.setConfiguration(config);

        annotationProcessorConfigurationService.addAnnotationProcessorPaths(
                config, processorDependencies, "Compiler");
    }

    /**
     * Disables annotation processing in the compiler plugin by setting proc=none.
     *
     * @param plugin the Maven Compiler plugin
     */
    public void disableCompilerAnnotationProcessing(Plugin plugin) {
        logger.debug("Disabling annotation processing in Maven Compiler...");

        // Configure all executions
        for (PluginExecution execution : plugin.getExecutions()) {
            Xpp3Dom config = xpp3DomService.getOrCreateConfiguration(execution.getConfiguration());
            execution.setConfiguration(config);

            Xpp3Dom proc = xpp3DomService.getOrCreateChild(config, "proc");
            proc.setValue("none");
            logger.debug("Set <proc>none</proc> in execution: " + execution.getId());
        }

        // Also configure at plugin level
        Xpp3Dom config = xpp3DomService.getOrCreateConfiguration(plugin.getConfiguration());
        plugin.setConfiguration(config);

        Xpp3Dom proc = xpp3DomService.getOrCreateChild(config, "proc");
        proc.setValue("none");
        logger.debug("Set <proc>none</proc> in compiler plugin configuration");
    }
}
