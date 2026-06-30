package com.wiredi.maven.services;

import com.wiredi.maven.LoggerWrapper;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for configuring annotation processor paths.
 */
public class AnnotationProcessorConfigurationService {

    private final Xpp3DomService xpp3DomService;
    private final DependencyService dependencyService;
    private static final LoggerWrapper logger = LoggerWrapper.getInstance();

    public AnnotationProcessorConfigurationService(Xpp3DomService xpp3DomService, DependencyService dependencyService) {
        this.xpp3DomService = xpp3DomService;
        this.dependencyService = dependencyService;
    }

    /**
     * Adds annotation processor paths to a configuration, checking for duplicates.
     *
     * @param config the configuration Xpp3Dom element
     * @param processorDependencies the processor dependencies to add
     * @param location the location description for logging
     */
    public void addAnnotationProcessorPaths(Xpp3Dom config, List<Dependency> processorDependencies, String location) {
        Xpp3Dom processorPaths = xpp3DomService.getOrCreateChild(config, "annotationProcessorPaths");

        int existingCount = processorPaths.getChildCount();
        logger.debug("Found " + existingCount + " existing annotation processor paths in " + location);

        // Build a set of existing dependencies for O(1) lookup
        Set<String> existingDependencies = new HashSet<>();
        for (int i = 0; i < existingCount; i++) {
            Xpp3Dom path = processorPaths.getChild(i);
            String key = dependencyService.createDependencyKey(path);
            if (key != null) {
                existingDependencies.add(key);
            }
        }

        for (Dependency dep : processorDependencies) {
            String depKey = dependencyService.createDependencyKey(dep);
            if (!existingDependencies.contains(depKey)) {
                dependencyService.addDependencyToXpp3Dom(processorPaths, dep);
                existingDependencies.add(depKey); // Add to set to prevent duplicates within the same batch
                logger.debug("Added to " + location + " annotationProcessorPaths: " +
                        dep.getGroupId() + ":" + dep.getArtifactId() + ":" + dep.getVersion());
            } else {
                logger.debug("Skipped (already present) in " + location + ": " +
                        dep.getGroupId() + ":" + dep.getArtifactId());
            }
        }
    }

    /**
     * Configures annotation processor paths without duplicate checking.
     *
     * @param processorPaths the processor paths Xpp3Dom element
     * @param processorDependencies the processor dependencies to add
     */
    public void configureAnnotationProcessorPaths(Xpp3Dom processorPaths, List<Dependency> processorDependencies) {
        for (Dependency dep : processorDependencies) {
            dependencyService.addDependencyToXpp3Dom(processorPaths, dep);
        }
    }
}
