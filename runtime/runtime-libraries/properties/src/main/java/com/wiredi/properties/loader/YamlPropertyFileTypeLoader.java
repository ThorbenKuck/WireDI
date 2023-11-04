package com.wiredi.properties.loader;

import com.google.auto.service.AutoService;
import com.wiredi.properties.keys.Key;
import com.wiredi.resources.Resource;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

@AutoService(PropertyFileTypeLoader.class)
public class YamlPropertyFileTypeLoader implements PropertyFileTypeLoader {

	private final Yaml yaml = new Yaml();

	@Override
	public Map<Key, String> extract(Resource resource) {
		Map<String, Object> load = yaml.load(resource.getInputStream());
		return flatten(load);
	}

	private Map<Key, String> flatten(Map<String, Object> input) {
		FlatteningContext flatteningContext = new FlatteningContext();
		flatten(input, flatteningContext);

		return flatteningContext.drain()
				.entrySet()
				.stream()
				.collect(
						Collectors.toMap(
								it -> Key.format(it.getKey()),
								it -> String.join(",", it.getValue())
						)
				);
	}

	private void flatten(Map<String, ?> collection, FlatteningContext context) {
		collection.forEach((key, value) -> {
			context.nextDepth(key, () -> flattenEntry(value, context));
		});
	}

	private void flattenEntry(Object instance, FlatteningContext context) {
		if (instance instanceof String) {
			context.appendValue((String) instance);
		} else if (instance instanceof Map<?, ?>) {
			flatten((Map<String, Object>) instance, context);
		} else if (instance instanceof List<?>) {
			((List<?>) instance).forEach(entry -> flattenEntry(entry, context));
		} else {
			throw new IllegalStateException("Unexpected element " + instance);
		}
	}

	class FlatteningContext {

		private final Map<String, List<String>> result = new HashMap<>();
		private final LinkedBlockingDeque<String> paths = new LinkedBlockingDeque<>();

		public String currentPath() {
			StringBuilder stringBuilder = new StringBuilder();
			paths.descendingIterator().forEachRemaining(entry -> {
				if(!stringBuilder.isEmpty()) {
					stringBuilder.append(".");
				}
				stringBuilder.append(entry);
			});
			return stringBuilder.toString();
		}

		public void nextDepth(String path, Runnable consumer) {
			paths.addFirst(path);
			try {
				consumer.run();
			} catch (Exception e) {
				throw new IllegalArgumentException("Error while flattening path: " + currentPath(), e);
			}
			paths.removeFirst();
		}

		public void appendValue(String value) {
			List<String> values = result.computeIfAbsent(currentPath(), (s) -> new ArrayList<>());
			values.add(value);
		}

		public Map<String, List<String>> drain() {
			HashMap<String, List<String>> value = new HashMap<>(result);
			paths.clear();
			result.clear();
			return value;
		}
	}

	@Override
	public List<String> supportedFileTypes() {
		return List.of("yaml", "yml");
	}
}
