package com.wiredi.maven;

import com.wiredi.maven.services.*;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Konfiguriert WireDi Annotation Processor Dependencies automatisch
 * und fügt sie den Compiler-Plugins hinzu.
 */
@Mojo(
        name = "configure",
        defaultPhase = LifecyclePhase.VALIDATE,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true,
        requiresProject = true
)
public class WireDiConfigureMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${plugin}", readonly = true, required = true)
    private PluginDescriptor pluginDescriptor;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    private RepositorySystemSession repoSession;

    @Component
    private RepositorySystem repositorySystem;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true)
    private List<RemoteRepository> remoteRepos;

    @Parameter(property = "wiredi.version")
    private String wirediVersion;

    @Parameter
    private List<ProcessorPlugin> compilerPlugins;

    @Parameter(property = "wiredi.useReleaseOnly", defaultValue = "false")
    private boolean useReleaseOnly;

    @Parameter(property = "wiredi.skip", defaultValue = "false")
    private boolean skip;

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
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().debug("WireDi configuration skipped");
            return;
        }

        if (wirediVersion == null || wirediVersion.trim().isEmpty()) {
            wirediVersion = pluginDescriptor.getVersion();
            getLog().debug("Using plugin version as WireDi version: " + wirediVersion);
        }

        getLog().debug("Configuring WireDi annotation processors...");

        List<Dependency> processorDependencies = collectProcessorDependencies();

        // Strategie 1: Dependencies zum Projekt hinzufügen (provided scope)
        addDependenciesToProject(processorDependencies);

        // Strategie 2: Kotlin KAPT Plugin konfigurieren
        if (!configureKotlinKaptPlugin(processorDependencies)) {
            // Strategie 3: Java Compiler Plugin konfigurieren
            configureJavaCompilerPlugin(processorDependencies);
        }

        getLog().debug("WireDi configuration completed successfully");
    }

    private void addDependenciesToProject(List<Dependency> dependencies) {
        for (Dependency dep : dependencies) {
            project.getDependencies().add(dep);
            getLog().debug("Added project dependency: " +
                    dep.getGroupId() + ":" + dep.getArtifactId() + ":" + dep.getVersion());
        }
    }

    private boolean configureKotlinKaptPlugin(List<Dependency> processorDependencies) throws MojoExecutionException {
        Optional<Plugin> kotlinPlugin = pluginFinderService.findPlugin(project, "org.jetbrains.kotlin", "kotlin-maven-plugin");

        if (kotlinPlugin.isEmpty()) {
            getLog().debug("Kotlin Maven Plugin not found - skipping KAPT configuration");
            return false;
        }

        return kaptConfigurationService.configureKaptPlugin(kotlinPlugin.get(), processorDependencies);
    }

    private void configureJavaCompilerPlugin(List<Dependency> processorDependencies) throws MojoExecutionException {
        Optional<Plugin> compilerPlugin = pluginFinderService.findPlugin(project, "org.apache.maven.plugins", "maven-compiler-plugin");

        if (compilerPlugin.isEmpty()) {
            getLog().debug("Maven Compiler Plugin not found - skipping configuration");
            return;
        }

        compilerConfigurationService.configureCompilerPlugin(compilerPlugin.get(), processorDependencies);
    }

    private List<Dependency> collectProcessorDependencies() throws MojoExecutionException {
        List<Dependency> dependencies = new ArrayList<>();

        // Core WireDi Processor
        Dependency coreProcessor = dependencyService.createDependency(
                "com.wiredi",
                "processors",
                wirediVersion,
                "provided"
        );
        dependencies.add(coreProcessor);

        // Zusätzliche Plugins
        if (compilerPlugins != null && !compilerPlugins.isEmpty()) {
            for (ProcessorPlugin plugin : compilerPlugins) {
                Dependency dep = plugin.toDependency(() -> resolveVersion(plugin));
                dependencies.add(dep);
            }
        }

        return dependencies;
    }

    private String resolveVersion(ProcessorPlugin plugin) throws MojoExecutionException {
        getLog().debug("Missing dependency version for " + plugin.getGroupId() + ":" + plugin.getArtifactId() + ". Resolving latest version.");
        String latestVersion = resolveLatestVersion(plugin.getGroupId(), plugin.getArtifactId());
        getLog().debug("Resolved latest version for " + plugin.getGroupId() + ":" +
                plugin.getArtifactId() + " = " + latestVersion);
        return latestVersion;
    }

    private String resolveLatestVersion(String groupId, String artifactId) throws MojoExecutionException {
        try {
            String versionRange = "[0,)";

            org.eclipse.aether.artifact.Artifact artifact = new DefaultArtifact(
                    groupId + ":" + artifactId + ":" + versionRange
            );

            VersionRangeRequest rangeRequest = new VersionRangeRequest();
            rangeRequest.setArtifact(artifact);
            rangeRequest.setRepositories(remoteRepos);

            VersionRangeResult rangeResult = repositorySystem.resolveVersionRange(repoSession, rangeRequest);

            List<org.eclipse.aether.version.Version> versions = rangeResult.getVersions();

            if (versions.isEmpty()) {
                throw new MojoExecutionException("No versions found for " + groupId + ":" + artifactId);
            }

            if (useReleaseOnly) {
                versions = versions.stream()
                        .filter(v -> !v.toString().contains("SNAPSHOT"))
                        .filter(v -> !v.toString().contains("alpha"))
                        .filter(v -> !v.toString().contains("beta"))
                        .filter(v -> !v.toString().contains("RC"))
                        .toList();
            }

            if (versions.isEmpty()) {
                throw new MojoExecutionException("No suitable versions found for " + groupId + ":" + artifactId);
            }

            return versions.get(versions.size() - 1).toString();

        } catch (VersionRangeResolutionException e) {
            throw new MojoExecutionException("Failed to resolve latest version for " +
                    groupId + ":" + artifactId, e);
        }
    }
}
