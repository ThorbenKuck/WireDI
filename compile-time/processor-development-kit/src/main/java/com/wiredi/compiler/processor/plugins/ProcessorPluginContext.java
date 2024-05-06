package com.wiredi.compiler.processor.plugins;

import com.wiredi.compiler.Injector;
import com.wiredi.runtime.collections.TypeMap;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.*;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.compiler.repository.CompilerRepositoryCallback;
import com.wiredi.runtime.domain.OrderedComparator;

import java.util.List;
import java.util.ServiceLoader;
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
            logger.info(() -> "Unhandled type " + type);
            wireProcessorPlugins.forEach(it -> it.handleUnsupported(type));
        };

        registerAttachListener(IdentifiableProviderEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        registerAttachListener(AspectAwareProxyEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        registerAttachListener(AspectHandlerEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        registerAttachListener(EnvironmentConfigurationEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
        registerAttachListener(WireBridgeEntity.class, entity -> wireProcessorPlugins.forEach(it -> it.handle(entity)));
    }

    public synchronized static <T extends Plugin> List<T> load(Injector injector, Class<T> type) {
        return (List<T>) cache.computeIfAbsent(type, () -> {
                    List<? extends T> content =
                            ServiceLoader.load(type)
                                    .stream()
                                    .map(provider -> injector.get(provider.type()))
                                    .sorted(OrderedComparator.INSTANCE)
                                    .peek(it -> {
                                        it.initialize();
                                        logger.debug(() -> "Initialized ProcessorPlugin: " + it.getClass().getSimpleName());
                                    })
                                    .collect(Collectors.toList());
                    logger.info(() -> "Loaded " + content.size() + " ProcessorPlugins: " + content.stream().map(it -> it.getClass().getSimpleName()).toList());
                    return content;
                }
        );
    }

    public <T extends ClassEntity<T>> void registerAttachListener(Class<T> type, Consumer<T> consumer) {
        attachListeners.put(type, (Consumer<ClassEntity<?>>) consumer);
    }

    public Consumer<ClassEntity<?>> getAttachListener(Class<?> type) {
        return attachListeners.getOrDefault(type, defaultFinalizeHandler);
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
