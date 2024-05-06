package com.wiredi.runtime.resources;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is a value object to hold protocol and path segments.
 *
 * @param protocol the protocol
 * @param path the path
 */
public record ResolverContext(@Nullable String protocol, @NotNull String path) {}
