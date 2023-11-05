package com.wiredi.resources;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ResolverContext(@NotNull String path, @Nullable String protocol) {}
