package com.wiredi.health;

import org.jetbrains.annotations.NotNull;

public interface HasHealth {

    @NotNull
    NamedHealth health();

}
