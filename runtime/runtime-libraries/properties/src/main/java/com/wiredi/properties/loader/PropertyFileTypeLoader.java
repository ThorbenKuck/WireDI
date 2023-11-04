package com.wiredi.properties.loader;

import com.wiredi.properties.keys.Key;
import com.wiredi.resources.Resource;

import java.util.List;
import java.util.Map;

/**
 * A loader that defines how to load certain properties, based on file extensions.
 * <p>
 * If you have a custom property format, you will need top provide a custom file loader that loads files for the
 * suffix provided in {@link #supportedFileTypes()}.
 * <p>
 * Please note: Any implementation of this class should be stateless.
 * It is loaded once globally and hence any state will stay for multiple instances of different WireRepositories.
 */
public interface PropertyFileTypeLoader {

    Map<Key, String> extract(Resource resource);

    List<String> supportedFileTypes();

}
