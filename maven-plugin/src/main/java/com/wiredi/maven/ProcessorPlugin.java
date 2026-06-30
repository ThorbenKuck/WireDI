package com.wiredi.maven;

import com.wiredi.runtime.lang.ThrowingSupplier;
import org.apache.maven.model.Dependency;

public class ProcessorPlugin {
    private String groupId;
    private String artifactId;
    private String version;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public <E extends Throwable> Dependency toDependency(ThrowingSupplier<String, E> defaultVersion) throws E {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version == null ? defaultVersion.get() : version);
        dependency.setScope("provided");
        return dependency;
    }
}