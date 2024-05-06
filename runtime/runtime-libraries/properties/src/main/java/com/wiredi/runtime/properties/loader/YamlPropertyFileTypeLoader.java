package com.wiredi.runtime.properties.loader;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.properties.TypeMapper;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.resources.Resource;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

/**
 * A {@link PropertyFileTypeLoader} that is able to resolve yaml files.
 * <p>
 * This loader completely flattens the contents of yaml files, which means that it joins different
 * levels of the yaml to individual property lines.
 */
@AutoService(PropertyFileTypeLoader.class)
public final class YamlPropertyFileTypeLoader implements PropertyFileTypeLoader {

    @NotNull
    private static final Yaml yaml = new Yaml();

    @Override
    public @NotNull Map<Key, String> extract(@NotNull final Resource resource) {
        @NotNull final Map<String, Object> load = yaml.load(resource.getInputStream());
        return flatten(load);
    }

    @NotNull
    private Map<Key, String> flatten(@NotNull final Map<String, Object> input) {
        @NotNull final FlatteningContext flatteningContext = new FlatteningContext();
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

    private void flatten(
            @NotNull final Map<String, ?> collection,
            @NotNull final FlatteningContext context
    ) {
        collection.forEach((key, value) -> {
            context.nextDepth(key, () -> flattenEntry(value, context));
        });
    }

    private void flattenEntry(
            @NotNull final Object instance,
            @NotNull final FlatteningContext context
    ) {
        switch (instance) {
            case Map<?, ?> map -> flatten((Map<String, Object>) map, context);
            case List<?> list -> list.forEach(entry -> flattenEntry(entry, context));
            default -> context.appendValue(TypeMapper.getInstance().stringify(instance));
        }
    }

    @Override
    public @NotNull List<String> supportedFileTypes() {
        return List.of("yaml", "yml");
    }

    private static final class FlatteningContext {

        @NotNull
        private final Map<String, List<String>> result = new HashMap<>();

        @NotNull
        private final LinkedBlockingDeque<String> paths = new LinkedBlockingDeque<>();

        @NotNull
        public String currentPath() {
            @NotNull final StringBuilder stringBuilder = new StringBuilder();
            paths.descendingIterator().forEachRemaining(entry -> {
                if (!stringBuilder.isEmpty()) {
                    stringBuilder.append(".");
                }
                stringBuilder.append(entry);
            });
            return stringBuilder.toString();
        }

        public void nextDepth(
                @NotNull final String path,
                @NotNull final Runnable consumer
        ) {
            paths.addFirst(path);
            try {
                consumer.run();
            } catch (@NotNull final Exception e) {
                throw new IllegalArgumentException("Error while flattening path: " + currentPath(), e);
            }
            paths.removeFirst();
        }

        public void appendValue(@NotNull final String value) {
            @NotNull final List<String> values = result.computeIfAbsent(currentPath(), (s) -> new ArrayList<>());
            values.add(value);
        }

        @NotNull
        public Map<String, List<String>> drain() {
            @NotNull final HashMap<String, List<String>> value = new HashMap<>(result);
            paths.clear();
            result.clear();
            return value;
        }
    }
}
