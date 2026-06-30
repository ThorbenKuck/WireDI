package com.wiredi.maven.services;

import com.wiredi.maven.LoggerWrapper;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service for configuring Kotlin KAPT annotation processing.
 */
public class KaptConfigurationService {

    private static final LoggerWrapper logger = LoggerWrapper.getInstance();
    private final Xpp3DomService xpp3DomService;
    private final AnnotationProcessorConfigurationService annotationProcessorConfigurationService;

    public KaptConfigurationService(Xpp3DomService xpp3DomService,
                                    AnnotationProcessorConfigurationService annotationProcessorConfigurationService) {
        this.xpp3DomService = xpp3DomService;
        this.annotationProcessorConfigurationService = annotationProcessorConfigurationService;
    }

    /**
     * Configures the Kotlin KAPT plugin with processor dependencies.
     * If a KAPT execution exists, it configures that. Otherwise, it configures at plugin level.
     * If no KAPT execution exists, it creates one.
     *
     * @param plugin                the Kotlin Maven plugin
     * @param processorDependencies the processor dependencies to add
     * @return true if KAPT was configured
     */
    public boolean configureKaptPlugin(Plugin plugin, List<Dependency> processorDependencies) {
        logger.debug("Found Kotlin Maven Plugin - configuring KAPT");

        List<PluginExecution> executions = plugin.getExecutions();
        logger.debug("Found " + executions.size() + " execution(s) in Kotlin Maven Plugin");

        for (PluginExecution exec : executions) {
            logger.debug("  Execution ID: " + exec.getId() + ", Goals: " + exec.getGoals());
        }

        // Try to find existing KAPT execution
        Optional<PluginExecution> kaptExecution = findKaptExecution(plugin);

        if (kaptExecution.isPresent()) {
            configureKaptExecution(kaptExecution.get(), processorDependencies);
        } else {
            // Create missing KAPT execution
            logger.debug("KAPT execution not found. Creating new KAPT execution.");
            PluginExecution newKaptExecution = createKaptExecution();
            plugin.addExecution(newKaptExecution);
            configureKaptExecution(newKaptExecution, processorDependencies);
        }

        return true;
    }

    /**
     * Finds an existing KAPT execution in the plugin.
     *
     * @param plugin the Kotlin Maven plugin
     * @return an Optional containing the KAPT execution if found
     */
    private Optional<PluginExecution> findKaptExecution(Plugin plugin) {
        return plugin.getExecutions().stream()
                .filter(exec -> exec.getGoals().contains("kapt") ||
                        "kapt".equals(exec.getId()) ||
                        exec.getId() != null && exec.getId().contains("kapt"))
                .findFirst();
    }

    /**
     * Creates a new KAPT execution with default configuration.
     *
     * @return the created KAPT execution
     */
    private PluginExecution createKaptExecution() {
        PluginExecution execution = new PluginExecution();
        execution.setId("kapt");
        execution.setGoals(Collections.singletonList("kapt"));
        return execution;
    }

    /**
     * Configures a KAPT execution with processor dependencies.
     *
     * @param execution             the KAPT execution
     * @param processorDependencies the processor dependencies to add
     */
    public void configureKaptExecution(PluginExecution execution, List<Dependency> processorDependencies) {
        logger.debug("Configuring KAPT execution: " + execution.getId());

        Xpp3Dom config = xpp3DomService.getOrCreateConfiguration(execution.getConfiguration());
        execution.setConfiguration(config);

        annotationProcessorConfigurationService.addAnnotationProcessorPaths(
                config, processorDependencies, "KAPT execution");
    }

    /**
     * Configures KAPT at plugin level with processor dependencies.
     *
     * @param plugin                the Kotlin Maven plugin
     * @param processorDependencies the processor dependencies to add
     */
    public void configurePluginLevelKapt(Plugin plugin, List<Dependency> processorDependencies) {
        logger.debug("Configuring KAPT at plugin level");

        Xpp3Dom config = xpp3DomService.getOrCreateConfiguration(plugin.getConfiguration());
        plugin.setConfiguration(config);

        annotationProcessorConfigurationService.addAnnotationProcessorPaths(
                config, processorDependencies, "KAPT plugin");
    }
}
