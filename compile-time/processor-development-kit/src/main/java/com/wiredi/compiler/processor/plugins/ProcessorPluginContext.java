package com.wiredi.compiler.processor.plugins;

import com.wiredi.compiler.Injector;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.*;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.compiler.repository.CompilerRepositoryCallback;
import com.wiredi.runtime.collections.TypeMap;
import com.wiredi.runtime.lang.OrderedComparator;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProcessorPluginContext implements CompilerRepositoryCallback {

    private static final Logger logger = Logger.get(ProcessorPluginContext.class);
    private static final TypeMap<List<? extends Plugin>> cache = new TypeMap<>();
    public final List<CompilerEntityPlugin> wireProcessorPlugins;
    private final TypeMap<Consumer<ClassEntity<?>>> attachListeners = new TypeMap<>();
    private final Consumer<ClassEntity<?>> defaultFinalizeHandler;

    public ProcessorPluginContext(Injector injector, CompilerRepository compilerRepository) {
        compilerRepository.registerCallback(this);

        wireProcessorPlugins = load(injector, CompilerEntityPlugin.class);
        defaultFinalizeHandler = (type) -> {
            logger.debug(() -> "Unhandled type " + type);
            wireProcessorPlugins.forEach(it -> it.handleUnsupported(type));
        };

        registerAttachListener(IdentifiableProviderEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        registerAttachListener(AspectAwareProxyEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        registerAttachListener(AspectHandlerEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        registerAttachListener(EnvironmentConfigurationEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        registerAttachListener(WireBridgeEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
    }

    private static final Map<Class<?>, List<?>> LOADED_CLASSES = new HashMap<>();

    public synchronized static <T extends Plugin> List<T> load(Injector injector, Class<T> type) {
        return (List<T>) LOADED_CLASSES.computeIfAbsent(type, t -> doLoad(injector, type));
    }

    private static <T extends Plugin> List<T> doLoad(Injector injector, Class<T> type) {
        List<T> result = ServiceLoader.load(type)
                .stream()
                .map(provider -> injector.get(provider.type()))
                .sorted(OrderedComparator.INSTANCE)
                .peek(it -> {
                    it.initialize();
                    logger.debug(() -> "Initialized ProcessorPlugin: " + it.getClass().getSimpleName());
                })
                .collect(Collectors.toList());
        logger.info(() -> "Loaded " + result.size() + " ProcessorPlugins: " + result.stream().map(it -> it.getClass().getSimpleName()).toList());

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
