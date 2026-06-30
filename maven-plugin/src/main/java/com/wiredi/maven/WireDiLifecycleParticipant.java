package com.wiredi.maven;

import com.wiredi.maven.services.*;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "wiredi")
public class WireDiLifecycleParticipant extends AbstractMavenLifecycleParticipant {

    private static final LoggerWrapper logger = LoggerWrapper.getInstance();

    // Services
    private final PluginFinderService pluginFinderService = new PluginFinderService();
    private final DependencyService dependencyService = new DependencyService();
    private final Xpp3DomService xpp3DomService = new Xpp3DomService();
    private final AnnotationProcessorConfigurationService annotationProcessorConfigurationService =
            new AnnotationProcessorConfigurationService(xpp3DomService, dependencyService);
    private final KaptConfigurationService kaptConfigurationService =
            new KaptConfigurationService(xpp3DomService, annotationProcessorConfigurationService);
    private final CompilerConfigurationService compilerConfigurationService =
            new CompilerConfigurationService(xpp3DomService, annotationProcessorConfigurationService);

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        for (MavenProject project : session.getProjects()) {
            try {
                configureProject(project);
            } catch (Exception e) {
                throw new MavenExecutionException("Failed to configure WireDi for project: " + project.getArtifactId(), e);
            }
        }
    }

    private void configureProject(MavenProject project) {
        // Finde WireDi Plugin
        Optional<Plugin> wirediPlugin = pluginFinderService.findPlugin(project, "com.wiredi", "maven-plugin");

        if (wirediPlugin.isEmpty()) {
            logger.debug(() -> "WireDi plugin not found in project: " + project.getArtifactId());
            return;
        }

        logger.info(() -> "Configuring project setup for wiredi");
        logger.debug(() -> "Configuring WireDi for project: " + project.getArtifactId());

        Plugin plugin = wirediPlugin.get();
        String wirediVersion = plugin.getVersion();

        // Lese compilerPlugins aus der Konfiguration
        List<ProcessorPlugin> compilerPlugins = readCompilerPlugins(plugin);

        // Sammle Dependencies
        List<Dependency> processorDependencies = new ArrayList<>();

        // Core Processor
        Dependency coreProcessor = dependencyService.createDependency("com.wiredi", "processors", wirediVersion, "provided");
        processorDependencies.add(coreProcessor);

        // Zusätzliche Plugins
        for (ProcessorPlugin compilerPlugin : compilerPlugins) {
            String version = compilerPlugin.getVersion();
            if (version == null || version.trim().isEmpty()) {
                version = wirediVersion; // Fallback
            }
            Dependency dep = dependencyService.createDependency(
                    compilerPlugin.getGroupId(),
                    compilerPlugin.getArtifactId(),
                    version,
                    "provided"
            );
            String finalVersion = version;
            logger.info(() -> "Configuring compiler plugin: " + dep.getGroupId() + ":" + dep.getArtifactId() + " in version " + finalVersion);
            processorDependencies.add(dep);
        }

        // Prüfe ob KAPT vorhanden ist
        boolean kaptConfigured = configureKaptPlugin(project, processorDependencies);

        if (kaptConfigured) {
            // KAPT gefunden: Nur zu KAPT hinzufügen, NICHT zum Projekt
            // Deaktiviere Annotation Processing im Compiler
            disableCompilerAnnotationProcessing(project);
            logger.debug(() -> "Using KAPT for annotation processing");
        } else {
            // Kein KAPT: Füge Dependencies zum Projekt hinzu UND konfiguriere Compiler
            for (Dependency dep : processorDependencies) {
                project.getDependencies().add(dep);
                logger.debug(() -> "Added dependency: " + dep.getGroupId() + ":" + dep.getArtifactId());
            }
            configureCompilerPlugin(project, processorDependencies);
            logger.debug(() -> "Using Maven Compiler for annotation processing");
        }
    }

    private void disableCompilerAnnotationProcessing(MavenProject project) {
        Optional<Plugin> compilerPlugin = pluginFinderService.findPlugin(project, "org.apache.maven.plugins", "maven-compiler-plugin");

        if (compilerPlugin.isEmpty()) {
            return;
        }

        compilerConfigurationService.disableCompilerAnnotationProcessing(compilerPlugin.get());
    }

    private List<ProcessorPlugin> readCompilerPlugins(Plugin wirediPlugin) {
        List<ProcessorPlugin> result = new ArrayList<>();

        Object configObj = wirediPlugin.getConfiguration();
        if (!(configObj instanceof Xpp3Dom)) {
            return result;
        }

        Xpp3Dom config = (Xpp3Dom) configObj;
        Xpp3Dom compilerPluginsNode = config.getChild("compilerPlugins");

        if (compilerPluginsNode == null) {
            return result;
        }

        for (Xpp3Dom pluginNode : compilerPluginsNode.getChildren("plugin")) {
            ProcessorPlugin dep = new ProcessorPlugin();
            
            Xpp3Dom groupId = pluginNode.getChild("groupId");
            Xpp3Dom artifactId = pluginNode.getChild("artifactId");
            Xpp3Dom version = pluginNode.getChild("version");

            if (groupId != null) dep.setGroupId(groupId.getValue());
            if (artifactId != null) dep.setArtifactId(artifactId.getValue());
            if (version != null) dep.setVersion(version.getValue());

            result.add(dep);
        }

        return result;
    }

    private boolean configureKaptPlugin(MavenProject project, List<Dependency> processorDependencies) {
        Optional<Plugin> kotlinPlugin = pluginFinderService.findPlugin(project, "org.jetbrains.kotlin", "kotlin-maven-plugin");

        if (kotlinPlugin.isEmpty()) {
            logger.debug("Kotlin Maven Plugin not found");
            return false;
        }

        return kaptConfigurationService.configureKaptPlugin(kotlinPlugin.get(), processorDependencies);
    }

    private void configureCompilerPlugin(MavenProject project, List<Dependency> processorDependencies) {
        Optional<Plugin> compilerPlugin = pluginFinderService.findPlugin(project, "org.apache.maven.plugins", "maven-compiler-plugin");

        if (compilerPlugin.isEmpty()) {
            logger.debug("Compiler plugin not found");
            return;
        }

        compilerConfigurationService.configureCompilerPlugin(compilerPlugin.get(), processorDependencies);
    }
}