package com.wiredi.compiler.repository;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.wiredi.compiler.domain.*;
import com.wiredi.compiler.domain.entities.*;
import com.wiredi.compiler.errors.ProcessingException;
import com.wiredi.compiler.logger.Logger;
import jakarta.inject.Inject;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class CompilerRepository {

    private static final Logger logger = Logger.get(CompilerRepository.class);

    private final Set<ClassEntity> classEntries = new HashSet<>();

    private final Map<String, WireBridgeEntity> wireBridgeEntities = new ConcurrentHashMap<>();

    private final Set<CompilerRepositoryCallback> repositoryCallbacks = new HashSet<>();

    @Inject
    private Filer filer;

    @Inject
    private Elements elements;
    @Inject
    private Types types;
    @Inject
    private WireRepositories wireRepositories;
    @Inject
    private TypeIdentifiers typeIdentifiers;

    public Elements getElements() {
        return elements;
    }

    public Types getTypes() {
        return types;
    }

    public void flush() {
        synchronized (classEntries) {
            if (!classEntries.isEmpty()) {
                logger.info(() -> "Flushing compiler repository with " + classEntries.size() + " classes in " + this);
            }
            classEntries.forEach(entry -> {
                if (!entry.isValid()) {
                    return;
                }
                repositoryCallbacks.forEach(it -> it.finalize(entry));
                TypeSpec typeSpec = entry.compile();
                var rootType = types.asElement(entry.rootType());
                PackageElement packageElement = (PackageElement) entry.packageElement().orElseGet(() -> elements.getPackageOf(rootType));

                logger.debug(() -> "Writing the file " + entry.className());
                try {
                    JavaFile.builder(packageElement.getQualifiedName().toString(), typeSpec)
                            .indent("    ")
                            .build()
                            .writeTo(filer);
                } catch (Exception e) {
                    throw new ProcessingException(rootType, "Failed to write the java file " + entry.className(), e);
                }
            });
            classEntries.clear();
        }
    }

    public void registerCallback(CompilerRepositoryCallback callback) {
        this.repositoryCallbacks.add(callback);
    }

    public EnvironmentConfigurationEntity newEnvironmentConfiguration(TypeElement typeElement) {
        return save(new EnvironmentConfigurationEntity(typeElement), entity -> attach(entity, typeElement));
    }

    public AspectHandlerEntity newAspectHandlerInstance(TypeElement typeElement, ExecutableElement executableElement) {
        return save(new AspectHandlerEntity(executableElement), entity -> attach(entity, typeElement).addSource(executableElement));
    }

    public IdentifiableProviderEntity newIdentifiableProvider(Element source, String name, TypeMirror typeMirror) {
        Element element = types.asElement(typeMirror);
        return save(new IdentifiableProviderEntity(source, typeMirror, name), entity -> attach(entity, element));
    }

    public IdentifiableProviderEntity newIdentifiableProvider(TypeElement typeElement) {
        return save(new IdentifiableProviderEntity(typeElement), entity -> attach(entity, typeElement).setPackageOf(typeElement));
    }

    public WireBridgeEntity newWireBridgeEntity(String caller, TypeElement typeElement) {
        synchronized (wireBridgeEntities) {
            return wireBridgeEntities.computeIfAbsent(caller + "$" + typeElement.getSimpleName() + "$WireBridge", name -> save(new WireBridgeEntity(typeElement, name, wireRepositories), entity -> attach(entity, typeElement).setPackageOf(typeElement)));
        }
    }

    public AspectAwareProxyEntity newAspectAwareProxy(TypeElement typeElement) {
        // TODO move asyncExecutionChainConstruction to properties
        return save(new AspectAwareProxyEntity(typeElement, typeIdentifiers, true), it -> attach(it, typeElement).setPackageOf(typeElement));
    }

    public <T extends ClassEntity<T>> T save(T entity, Consumer<T> consumer) {
        logger.debug("Saving " + entity + " to " + this);
        synchronized (classEntries) {
            this.classEntries.add(entity);
            consumer.accept(entity);
            repositoryCallbacks.forEach(it -> it.saved(entity));
        }
        return entity;
    }

    public <T extends ClassEntity<T>> T save(T entity) {
        logger.debug("Saving " + entity + " to " + this);
        synchronized (classEntries) {
            this.classEntries.add(entity);
            repositoryCallbacks.forEach(it -> it.saved(entity));
        }
        return entity;
    }

    private <T extends AbstractClassEntity<T>> T attach(T entity, Element typeElement) {
        logger.debug(() -> "Attaching " + entity);
        return entity.setPackage(TypeUtils.packageOf(typeElement))
                .addSource(typeElement);
    }

    @Override
    public String toString() {
        return "CompilerRepository{" +
                "classEntries=" + classEntries +
                '}';
    }
}
