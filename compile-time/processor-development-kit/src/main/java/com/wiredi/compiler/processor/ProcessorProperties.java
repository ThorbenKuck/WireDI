package com.wiredi.compiler.processor;

import com.wiredi.compiler.processor.lang.AnnotationProcessorResource;
import com.wiredi.runtime.properties.PropertyLoader;
import com.wiredi.runtime.properties.TypedProperties;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.properties.loader.PropertiesPropertyFileTypeLoader;
import com.wiredi.runtime.properties.loader.YamlPropertyFileTypeLoader;

import javax.annotation.processing.Filer;
import java.util.*;
import java.util.stream.Collectors;

public class ProcessorProperties {

	private static final String FILE_NAME = "wire-di.processor.properties";
	private static final PropertyLoader propertyLoader = new PropertyLoader(new PropertiesPropertyFileTypeLoader(), new YamlPropertyFileTypeLoader());
	private final TypedProperties properties;

	public ProcessorProperties(Filer filer) {
		properties = propertyLoader.safeLoad(new AnnotationProcessorResource(filer, FILE_NAME));
	}

	public void addOptions(Map<String, String> options) {
		this.properties.setAll(options.entrySet().stream().collect(Collectors.toMap(it -> Key.format(it.getKey()), Map.Entry::getValue)));
	}

	public boolean isEnabled(PropertyKeys propertyKey) {
		return properties.getBoolean(propertyKey.getRawKey(), toBool(propertyKey.getDefaultValue(), false));
	}

	public boolean isDisabled(PropertyKeys propertyKey) {
		return !isEnabled(propertyKey);
	}

	public int getCount(PropertyKeys propertyKey, int defaultValue) {
		return properties.getInt(propertyKey.getRawKey(), defaultValue);
	}

	public String getName(PropertyKeys extensionFileName) {
		return properties.require(extensionFileName.getRawKey());
	}

	public List<String> getAll(PropertyKeys propertyKey) {
		return properties.getAll(propertyKey.getRawKey());
	}

	public TypedProperties getSource() {
		return properties;
	}

	private boolean toBool(Object object, boolean def) {
		if (object instanceof Boolean) {
			return (Boolean) object;
		} else if (Objects.equals(object.getClass(), boolean.class)) {
			return (boolean) object;
		} else {
			return def;
		}
	}
}
