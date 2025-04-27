package com.wiredi.jackson;

import com.wiredi.runtime.collections.pages.Page;
import org.jetbrains.annotations.NotNull;

public record Response(@NotNull Page<Integer> page) {

}
