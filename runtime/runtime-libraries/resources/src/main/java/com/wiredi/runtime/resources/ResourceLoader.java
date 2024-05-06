package com.wiredi.runtime.resources;

import com.wiredi.runtime.resources.builtin.ClassPathResourceProtocolResolver;
import com.wiredi.runtime.resources.builtin.UnsupportedResourceProtocolResolver;
import com.wiredi.runtime.resources.builtin.FileSystemResourceProtocolResolver;
import com.wiredi.runtime.resources.exceptions.UnsupportedResourceProtocolException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A ResourceLoader allows you to resolve and load Resources.
 * <p>
 * It holds multiple {@link ResourceProtocolResolver resolvers}.
 * These resolvers are used to find Resources.
 * <p>
 * A resource can be referenced like this: {@code <protocol>:<url>}.
 * So if you want to reference a classpath resource, you would write: {@code classpath:my.properties}.
 * If a resource is loaded using this class, the ResourceLoaded tries to use a {@link ResourceProtocolResolver}
 * correlating to the provided protocol.
 * <p>
 * In this example, this would mean that a {@link ResourceProtocolResolver} is required, which can resolve classpath.
 * For this, the {@link ClassPathResourceProtocolResolver} can be used.
 * All together, it would look like this:
 *
 * <pre><code>
 * public class MyService {
 *     	private final ResourceLoader resourceLoader = ResourceLoader.open(ClassPathResourceProtocolResolver.INSTANCE);
 *
 *     	public String loadPropertyFile() {
 *     	    return resourceLoader.load("classpath:my.properties")
 *     	                .getContentAsString()
 *      }
 * }
 * </code></pre>
 * <p>
 * If this {@link ResourceLoader} is not able to resolve the provided protocol,
 * a default {@link ResourceProtocolResolver} is asked to resolve the {@link Resource}.
 * This can be specified by overwriting the default resolver whilst constructing this ResourceLoader,
 * or by calling {@link #setDefaultResolver(ResourceProtocolResolver)}.
 * If not specified otherwise, the {@link UnsupportedResourceProtocolResolver} is used,
 * that throws an {@link UnsupportedResourceProtocolException}.
 *
 * @see Resource
 * @see ResourceProtocolResolver
 * @see UnsupportedResourceProtocolResolver
 * @see ClassPathResourceProtocolResolver
 * @see FileSystemResourceProtocolResolver
 */
public class ResourceLoader {

    private static final char PROTOCOL_DELIMITER = ':';
    @NotNull
    private final Map<@NotNull String, @NotNull ResourceProtocolResolver> protocolResolvers;
    @NotNull
    private ResourceProtocolResolver defaultResolver;

    public ResourceLoader() {
        this(Collections.emptyList());
    }

    public ResourceLoader(@NotNull final Collection<@NotNull ResourceProtocolResolver> resolvers) {
        this(resolvers, UnsupportedResourceProtocolResolver.INSTANCE);
    }

    public ResourceLoader(
            @NotNull final Collection<@NotNull ResourceProtocolResolver> resolvers,
            @NotNull ResourceProtocolResolver defaultResolver
    ) {
        this(new HashMap<>(), defaultResolver);
        resolvers.forEach(this::addProtocolResolver);
    }

    public ResourceLoader(
            @NotNull final Map<@NotNull String, @NotNull ResourceProtocolResolver> resolvers,
            @NotNull ResourceProtocolResolver defaultResolver
    ) {
        this.protocolResolvers = resolvers;
        this.defaultResolver = defaultResolver;
    }

    /**
     * Constructs a new ResourceLoader with the provided {@link ResourceProtocolResolver}.
     *
     * @param resolvers all Resolvers that this ResourceLoader should support
     * @return a new ResourceLoader instance with the provided resolvers
     */
    @NotNull
    public static ResourceLoader open(final ResourceProtocolResolver... resolvers) {
        return new ResourceLoader(Arrays.asList(resolvers));
    }

    /**
     * Sets the default resolver, which is used to resolve a {@link Resource}
     * if no other {@link ResourceProtocolResolver} could be found for a given protocol.
     *
     * @param defaultResolver the {@link ResourceProtocolResolver} to use if no other can be found.
     * @return this instance for functional style call chains
     * @see UnsupportedResourceProtocolResolver
     */
    @NotNull
    public ResourceLoader setDefaultResolver(@NotNull ResourceProtocolResolver defaultResolver) {
        this.defaultResolver = Objects.requireNonNull(defaultResolver);

        return this;
    }

    /**
     * Adds all the provided {@link ResourceProtocolResolver} instances to this {@link ResourceLoader}.
     * <p>
     * All individual {@link ResourceProtocolResolver}
     * where added using {@link #addProtocolResolver(ResourceProtocolResolver)}
     *
     * @param protocolResolvers the {@link ResourceProtocolResolver} to add
     * @return this instance for functional style call chains
     * @see #addProtocolResolver(ResourceProtocolResolver)
     */
    public ResourceLoader addProtocolResolvers(@NotNull final Collection<? extends ResourceProtocolResolver> protocolResolvers) {
        protocolResolvers.forEach(this::addProtocolResolver);

        return this;
    }

    /**
     * Adds the provided {@link ResourceProtocolResolver} to this instance.
     * <p>
     * If any protocol is already registered by another {@link ResourceProtocolResolver}, an Exception is raised.
     * If any protocol already was registered by another {@link ResourceProtocolResolver},
     * the {@link ResourceProtocolResolver} will not be registered.
     *
     * @param resourceProtocolResolver the {@link ResourceProtocolResolver} to register.
     * @return this instance for functional style call chains
     */
    @NotNull
    public ResourceLoader addProtocolResolver(@NotNull final ResourceProtocolResolver resourceProtocolResolver) {
        Objects.requireNonNull(resourceProtocolResolver);
        resourceProtocolResolver.supportedProtocols().forEach(type -> {
            if (protocolResolvers.containsKey(type)) {
                throw new IllegalArgumentException("The ProtocolResolver " + resourceProtocolResolver + " tried to register the type " + type + " but there already is a ProtocolResolver registered for it");
            }
        });
        resourceProtocolResolver.supportedProtocols().forEach(type -> protocolResolvers.put(type, resourceProtocolResolver));

        return this;
    }

    /**
     * Sets the provided {@link ResourceProtocolResolver} to this instance.
     * <p>
     * Contrary to {@link #addProtocolResolver(ResourceProtocolResolver)}, this method will override any previously
     * registered {@link ResourceProtocolResolver} for all given {@link ResourceProtocolResolver#supportedProtocols()}
     *
     * @param resourceProtocolResolver the resolver to add
     * @return this instance for functional style call chains
     */
    @NotNull
    public ResourceLoader setProtocolResolver(@NotNull final ResourceProtocolResolver resourceProtocolResolver) {
        Objects.requireNonNull(resourceProtocolResolver);
        resourceProtocolResolver.supportedProtocols().forEach(type -> protocolResolvers.put(type, resourceProtocolResolver));

        return this;
    }

    /**
     * Loads a {@link Resource} from the given path.
     * <p>
     * This method will resolve the protocol of the path and then chose one {@link ResourceProtocolResolver} to
     * resolve the {@link Resource}.
     * If the protocol could not be resolved (which likely means that it did not contain any)
     * the {@link #defaultResolver} is asked to resolve the resource.
     * <p>
     * If the {@link ResourceProtocolResolver} chosen for this path was unable to resolve a {@link Resource},
     * the {@link #defaultResolver} is asked to resolve the resource.
     *
     * @param path the path to resolve
     * @return a {@link Resource} of the protocol specified in the path.
     */
    @NotNull
    public Resource load(@NotNull final String path) {
        final ResolverContext resolverContext = determinePathWithProtocol(path, null);
        if (resolverContext.protocol() == null) {
            return defaultResolver.resolve(resolverContext);
        }

        return Optional.ofNullable(protocolResolvers.get(resolverContext.protocol()))
                .map(resolver -> resolver.resolve(resolverContext))
                .orElseGet(() -> defaultResolver.resolve(resolverContext));
    }

    /**
     * Loads a {@link Resource} from the given path.
     * <p>
     * This method will resolve the protocol of the path and then chose one {@link ResourceProtocolResolver} to
     * resolve the {@link Resource}.
     * If the protocol could not be resolved (which likely means that it did not contain any),
     * the defaultProtocol is used.
     * <p>
     * If the {@link ResourceProtocolResolver} chosen for this path was unable to resolve a {@link Resource},
     * the {@link #defaultResolver} is asked to resolve the resource.
     *
     * @param path the path to resolve
     * @return a {@link Resource} of the protocol specified in the path.
     */
    @NotNull
    public Resource load(@NotNull final String path, final String defaultProtocol) {
        final ResolverContext resolverContext = determinePathWithProtocol(path, defaultProtocol);
        if (resolverContext.protocol() == null) {
            return defaultResolver.resolve(resolverContext);
        }

        return Optional.ofNullable(protocolResolvers.get(resolverContext.protocol()))
                .map(resolver -> resolver.resolve(resolverContext))
                .orElseGet(() -> defaultResolver.resolve(resolverContext));
    }

    /**
     * Determines the protocol and relative path of the provided url.
     * <p>
     * A correct url should look like this: {@code <resource>:<url>}.
     * <p>
     * It splits the url at the {@link #PROTOCOL_DELIMITER},
     * where the first segment is the protocol and the second is the relative path.
     *
     * @param url             the url which should be analyzed for protocol
     * @param defaultProtocol (optional) a protocol to use, if the url does not contain a {@link #PROTOCOL_DELIMITER}
     * @return a new {@link ResolverContext} containing the path and the protocol.
     */
    @NotNull
    public ResolverContext determinePathWithProtocol(@NotNull final String url, @Nullable final String defaultProtocol) {
        final int index = url.indexOf(PROTOCOL_DELIMITER);
        if (index == -1) {
            return new ResolverContext(defaultProtocol, url);
        } else {
            final String protocol = url.substring(0, index);
            final String result = url.substring(index + 1);
            return new ResolverContext(protocol, result);
        }
    }

    public void clear() {
        protocolResolvers.clear();
        defaultResolver = UnsupportedResourceProtocolResolver.INSTANCE;
    }
}
