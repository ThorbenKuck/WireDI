package com.wiredi.processor.plugins;

import com.wiredi.Injector;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.*;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.compiler.repository.CompilerRepositoryCallback;
import com.wiredi.domain.OrderComparator;
import com.wiredi.lang.collections.TypeMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProcessorPluginContext implements CompilerRepositoryCallback {

    private static final Logger logger = Logger.get(ProcessorPluginContext.class);
    private static final TypeMap<List<? extends Plugin>> cache = new TypeMap<>();
    private final TypeMap<Consumer<ClassEntity<?>>> invokers = new TypeMap<>();
    public final List<CompilerEntityPlugin> wireProcessorPlugins;
    private final Consumer<ClassEntity<?>> defaultHandler;

    public ProcessorPluginContext(Injector injector, CompilerRepository compilerRepository) {
        wireProcessorPlugins = ProcessorPluginContext.load(injector, CompilerEntityPlugin.class);
        defaultHandler = (type) -> {
            logger.info(() -> "Unhandled type " + type);
            wireProcessorPlugins.forEach(it -> it.handleUnsupported(type));
        };
        compilerRepository.registerCallback(this);

        register(IdentifiableProviderEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        register(AspectAwareProxyEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        register(AspectHandlerEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        register(EnvironmentConfigurationEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        register(WireBridgeEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
    }

    public synchronized static <T extends Plugin> List<T> load(Injector injector, Class<T> type) {
        return (List<T>) cache.computeIfAbsent(type, () -> {
                    List<? extends T> content = ServiceLoader.load(type)
                            .stream()
                            .map(provider -> injector.get(provider.type()))
                            .sorted(OrderComparator.INSTANCE)
                            .peek(it -> {
                                it.initialize();
                                logger.debug(() -> "Initialized ProcessorPlugin: " + it.getClass().getSimpleName());
                            })
                            .collect(Collectors.toList());
                    logger.debug(() -> "ProcessorPluginLoader configured withLoaded " + content.size() + " plugins");
                    return content;
                }
        );
    }

    public <T extends ClassEntity<T>> void register(Class<T> type, Consumer<T> consumer) {
        invokers.put(type, (Consumer<ClassEntity<?>>) consumer);
    }

    public Consumer<ClassEntity<?>> getHandlerOf(Class<?> type) {
        return invokers.getOrDefault(type, defaultHandler);
    }

    @Override
    public void finalize(ClassEntity<?> classEntity) {
        getHandlerOf(classEntity.getClass()).accept(classEntity);
    }
}
