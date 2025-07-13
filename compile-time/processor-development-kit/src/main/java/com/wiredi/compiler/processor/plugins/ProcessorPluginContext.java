package com.wiredi.compiler.processor.plugins;

import com.wiredi.compiler.Injector;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.*;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.compiler.repository.CompilerRepositoryCallback;
import com.wiredi.runtime.collections.TypeMap;
import com.wiredi.runtime.lang.Ordered;
import com.wiredi.runtime.lang.OrderedComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProcessorPluginContext implements CompilerRepositoryCallback {

    private static final Logger logger = LoggerFactory.getLogger(ProcessorPluginContext.class);
    public final List<CompilerEntityPlugin> wireProcessorPlugins = new ArrayList<>();
    private final TypeMap<Consumer<ClassEntity<?>>> attachListeners = new TypeMap<>();
    private final Consumer<ClassEntity<?>> defaultFinalizeHandler;

    public ProcessorPluginContext(Injector injector, CompilerRepository compilerRepository) {
        compilerRepository.registerCallback(this);

        load(injector, CompilerEntityPluginFactory.class).stream()
                .map(factory -> factory.create(injector))
                .forEach(wireProcessorPlugins::add);
        wireProcessorPlugins.addAll(load(injector, CompilerEntityPlugin.class));
        OrderedComparator.sort(wireProcessorPlugins);

        defaultFinalizeHandler = (type) -> {
            logger.debug("Unhandled type {}", type);
            wireProcessorPlugins.forEach(it -> it.handleUnsupported(type));
        };

        registerAttachListener(IdentifiableProviderEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        registerAttachListener(AspectAwareProxyEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        registerAttachListener(AspectHandlerEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        registerAttachListener(EnvironmentConfigurationEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        registerAttachListener(WireBridgeEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        wireProcessorPlugins.forEach(it -> it.initialize(injector));
    }

    private static final Map<Class<?>, List<?>> LOADED_CLASSES = new HashMap<>();

    public synchronized static <T extends Ordered> List<T> load(Injector injector, Class<T> type) {
        return (List<T>) LOADED_CLASSES.computeIfAbsent(type, t -> doLoad(injector, type));
    }

    private static <T extends Ordered> List<T> doLoad(Injector injector, Class<T> type) {
        List<T> result = ServiceLoader.load(type)
                .stream()
                .map(provider -> {
                    try {
                        return injector.get(provider.type());
                    } catch (Throwable throwable) {
                        logger.warn("Failed to load {}: {}", type.getSimpleName(), throwable.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted(OrderedComparator.INSTANCE)
                .collect(Collectors.toList());
        logger.info("Loaded {}({}): {}", type.getSimpleName(), result.size(), result.stream().map(it -> it.getClass().getSimpleName()).toList());

        return result;

    }

    public <T extends ClassEntity<T>> void registerAttachListener(Class<T> type, Consumer<T> consumer) {
        attachListeners.put(type, (Consumer<ClassEntity<?>>) consumer);
    }

    public Consumer<ClassEntity<?>> getAttachListener(Class<?> type) {
        return attachListeners.getOrDefault(type, defaultFinalizeHandler);
    }

    public void forEach(Consumer<CompilerEntityPlugin> consumer) {
        wireProcessorPlugins.forEach(consumer);
    }

    @Override
    public void finalize(ClassEntity<?> classEntity) {
        wireProcessorPlugins.forEach(it -> it.onFlush(classEntity));
    }

    @Override
    public void saved(ClassEntity<?> classEntity) {
        getAttachListener(classEntity.getClass()).accept(classEntity);
    }
}
