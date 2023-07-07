package com.wiredi.resources;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PathWithProtocol(@NotNull String path, @Nullable String protocol) {}
