package com.wiredi.maven.services;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

import java.util.Optional;

/**
 * Service for finding Maven plugins in a project.
 */
public class PluginFinderService {

    /**
     * Finds a plugin in the project's build plugins.
     *
     * @param project the Maven project
     * @param groupId the plugin group ID
     * @param artifactId the plugin artifact ID
     * @return an Optional containing the plugin if found
     */
    public Optional<Plugin> findPlugin(MavenProject project, String groupId, String artifactId) {
        return project.getBuildPlugins().stream()
                .filter(p -> groupId.equals(p.getGroupId()) && artifactId.equals(p.getArtifactId()))
                .findFirst();
    }
}
