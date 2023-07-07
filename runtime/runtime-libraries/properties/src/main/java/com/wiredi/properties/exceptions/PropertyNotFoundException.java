package com.wiredi.properties.exceptions;

import com.wiredi.properties.keys.Key;
import org.jetbrains.annotations.NotNull;

public class PropertyNotFoundException extends RuntimeException {

    public PropertyNotFoundException(@NotNull final String key)  {
        super("Could not find the property \"" + key + "\"");
    }

    public PropertyNotFoundException(@NotNull final Key key)  {
        this(key.value());
    }

    public PropertyNotFoundException(
            @NotNull final String key,
            @NotNull final String source
    )  {
        super("Could not find the property \"" + key + "\" in \"" + source + "\"");
    }
}
