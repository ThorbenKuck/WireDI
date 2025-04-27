package com.wiredi.jackson;

import com.wiredi.runtime.collections.pages.Pageable;
import org.jetbrains.annotations.Nullable;

public record Request(@Nullable Pageable pageable) {

}
