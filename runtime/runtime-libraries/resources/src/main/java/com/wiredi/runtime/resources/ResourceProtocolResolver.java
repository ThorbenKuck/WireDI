package com.wiredi.runtime.resources;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Support for creating custom Resource types based on paths.
 * <p>
 * A ResourceProtocolResolver will work with string, that are looking like this: <pre><code>type:path</code></pre>.
 * "type" in this is the concrete protocol type to resolve. "path" is then the concrete path to resolve as a Resource.
 * <p>
 * One example might look like this: <pre><code>classpath:my.properties</code></pre>. In this the ResourceProtocol is
 * "classpath" and the path is "my.properties".
 * <p>
 * <h2>Example</h2>
 * If you want to construct a custom remote resource, you would need to implement the {@link Resource} interface and
 * then a custom protocol resolver.
 * <p>
 * Let's say you want to have the mentioned remote resource be accessible if the resource name starts with "remote"
 * (i.e. "remote:https://my.url/path/to/resource"), then you will need to provide a ResourceProtocolResolver which
 * contains "remote" in its {@link #supportedProtocols()}
 * <p>
 * Implementation Note: This class should be stateless.
 */
public interface ResourceProtocolResolver {

    /**
     * Tries to resolve the provided path.
     * <p>
     * The original resource type will no longer be in the path. So if your custom ResourceProtocolResolver will handle
     * the "remote" type, and the input was: <pre><code>remote:https://my.url/path/to/resource</code></pre>, the
     * provided path will be <pre><code>https://my.url/path/to/resource</code></pre>.
     *
     * @param resolverContext the context containing the path and protocol
     * @return A resource that is pointing to the provided path
     */
    @NotNull
    Resource resolve(@NotNull final ResolverContext resolverContext);

    /**
     * All types that this ResourceProtocolResolver supports.
     *
     * @return all protocols that this resolver supports
     */
    @NotNull
    Set<String> supportedProtocols();

}
