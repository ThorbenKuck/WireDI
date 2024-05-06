package com.wiredi.runtime.properties.loader;

import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.resources.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * A loader that defines how to load certain properties, based on file extensions.
 * <p>
 * If you have a custom property format, you will need to provide a custom file loader that loads files for the
 * suffix provided in {@link #supportedFileTypes()}.
 * <p>
 * Please note: Any implementation of this class should be stateless.
 */
public interface PropertyFileTypeLoader {

    /**
     * Converts the provided resource to a map containing all entries in the properties
     *
     * @param resource the resource that holds the property
     * @return a new map, flat mapped, with all entries of the {@link Resource}
     */
    @NotNull
    Map<Key, String> extract(@NotNull Resource resource);

    @NotNull
    List<String> supportedFileTypes();

}
