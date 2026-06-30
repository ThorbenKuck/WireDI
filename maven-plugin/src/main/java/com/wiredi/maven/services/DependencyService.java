package com.wiredi.maven.services;

import org.apache.maven.model.Dependency;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Service for creating and managing Maven dependencies.
 */
public class DependencyService {

    /**
     * Creates a Maven dependency with the specified coordinates and scope.
     *
     * @param groupId the group ID
     * @param artifactId the artifact ID
     * @param version the version
     * @param scope the scope
     * @return the created dependency
     */
    public Dependency createDependency(String groupId, String artifactId, String version, String scope) {
        Dependency dep = new Dependency();
        dep.setGroupId(groupId);
        dep.setArtifactId(artifactId);
        dep.setVersion(version);
        dep.setScope(scope);
        return dep;
    }

    /**
     * Adds a dependency to an Xpp3Dom element as a child.
     *
     * @param parent the parent Xpp3Dom element
     * @param dep the dependency to add
     */
    public void addDependencyToXpp3Dom(Xpp3Dom parent, Dependency dep) {
        Xpp3Dom dependency = new Xpp3Dom("dependency");

        Xpp3Dom groupId = new Xpp3Dom("groupId");
        groupId.setValue(dep.getGroupId());
        dependency.addChild(groupId);

        Xpp3Dom artifactId = new Xpp3Dom("artifactId");
        artifactId.setValue(dep.getArtifactId());
        dependency.addChild(artifactId);

        Xpp3Dom version = new Xpp3Dom("version");
        version.setValue(dep.getVersion());
        dependency.addChild(version);

        parent.addChild(dependency);
    }

    /**
     * Creates a unique key for a dependency based on groupId and artifactId.
     *
     * @param dependency the dependency
     * @return a unique key in the format "groupId:artifactId"
     */
    public String createDependencyKey(Dependency dependency) {
        return dependency.getGroupId() + ":" + dependency.getArtifactId();
    }

    /**
     * Creates a unique key for a dependency from an Xpp3Dom element.
     *
     * @param dependencyElement the Xpp3Dom element representing a dependency
     * @return a unique key in the format "groupId:artifactId", or null if required elements are missing
     */
    public String createDependencyKey(Xpp3Dom dependencyElement) {
        Xpp3Dom groupId = dependencyElement.getChild("groupId");
        Xpp3Dom artifactId = dependencyElement.getChild("artifactId");

        if (groupId != null && artifactId != null) {
            return groupId.getValue() + ":" + artifactId.getValue();
        }

        return null;
    }

    /**
     * Checks if a dependency is already present in the processor paths.
     *
     * @param processorPaths the processor paths Xpp3Dom element
     * @param dep the dependency to check
     * @return true if the dependency is already present
     */
    public boolean isDependencyAlreadyPresent(Xpp3Dom processorPaths, Dependency dep) {
        for (Xpp3Dom child : processorPaths.getChildren()) {
            if (!"dependency".equals(child.getName()) && !"path".equals(child.getName())) {
                continue;
            }

            Xpp3Dom groupIdNode = child.getChild("groupId");
            Xpp3Dom artifactIdNode = child.getChild("artifactId");

            if (groupIdNode != null && artifactIdNode != null) {
                String groupId = groupIdNode.getValue();
                String artifactId = artifactIdNode.getValue();

                if (dep.getGroupId().equals(groupId) && dep.getArtifactId().equals(artifactId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
