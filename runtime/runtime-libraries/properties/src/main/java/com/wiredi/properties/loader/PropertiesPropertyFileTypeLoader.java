package com.wiredi.properties.loader;

import com.google.auto.service.AutoService;
import com.wiredi.properties.exceptions.PropertyLoadingException;
import com.wiredi.properties.keys.Key;
import com.wiredi.resources.Resource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

@AutoService(PropertyFileTypeLoader.class)
public final class PropertiesPropertyFileTypeLoader implements PropertyFileTypeLoader {
	@Override
	public @NotNull Map<Key, String> extract(Resource resource) {
		Properties properties = new Properties();
		try {
			properties.load(resource.getInputStream());
		} catch (IOException e) {
			throw new PropertyLoadingException(resource.getFilename(), e);
		}
		Map<Key, String> result = new HashMap<>();
		properties.stringPropertyNames().forEach(key -> {
			result.put(Key.format(key), properties.getProperty(key));
		});
		properties.clear();
		return result;
	}

	@Override
	public @NotNull List<String> supportedFileTypes() {
		return List.of("properties");
	}
}
