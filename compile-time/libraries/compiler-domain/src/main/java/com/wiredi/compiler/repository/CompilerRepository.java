package com.wiredi.compiler.repository;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.wiredi.compiler.domain.*;
import com.wiredi.compiler.domain.entities.AspectAwareProxyEntity;
import com.wiredi.compiler.domain.entities.EnvironmentConfigurationEntity;
import com.wiredi.compiler.domain.entities.IdentifiableProviderEntity;
import com.wiredi.compiler.domain.entities.WireBridgeEntity;
import com.wiredi.compiler.errors.ProcessingException;
import com.wiredi.compiler.logger.Logger;
import jakarta.inject.Inject;
import kotlin.jvm.Synchronized;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CompilerRepository {

	private final List<ClassEntity> classEntries = new ArrayList<>();

	private final Map<String, WireBridgeEntity> wireBridgeEntities = new HashMap<>();

	@Inject
	private Filer filer;

	@Inject
	private Elements elements;

	@Inject
	private Types types;

	@Inject
	private TypeIdentifiers typeIdentifiers;

	@Inject
	private WireRepositories wireRepositories;

	private final Logger logger = Logger.get(CompilerRepository.class);

	public void flush() {
		synchronized (classEntries) {
			classEntries.forEach(entry -> {
				TypeSpec typeSpec = entry.build();
				var rootType = types.asElement(entry.rootType());
				PackageElement packageElement = entry.packageElement().orElseGet(() -> elements.getPackageOf(rootType));

				try {
					JavaFile.builder(packageElement.getQualifiedName().toString(), typeSpec)
							.indent("    ")
							.build()
							.writeTo(filer);
				} catch (Exception e) {
					throw new ProcessingException(rootType, "Failed to write the java file " + entry.className(), e);
				}
			});
		}
	}

	public <T extends ClassEntity> T save(T entity, Consumer<T> consumer) {
		synchronized (classEntries) {
			this.classEntries.add(entity);
			consumer.accept(entity);
		}
		return entity;
	}

	public <T extends ClassEntity> T save(T entity) {
		return save(entity, (it) -> {});
	}

	public EnvironmentConfigurationEntity newEnvironmentConfiguration(TypeElement typeElement) {
		return save(new EnvironmentConfigurationEntity(typeElement), entity -> attach(entity, typeElement));
	}

	public IdentifiableProviderEntity newIdentifiableProvider(String name, TypeMirror typeMirror) {
		Element element = types.asElement(typeMirror);
		return save(create(typeMirror, name), entity -> attach(entity, element));
	}

	public IdentifiableProviderEntity newIdentifiableProvider(TypeElement typeElement) {
		return save(create(typeElement), entity -> attach(entity, typeElement).setPackageOf(typeElement));
	}

	public WireBridgeEntity newWireBridgeEntity(String caller, TypeElement typeElement) {
		synchronized(wireBridgeEntities) {
			return wireBridgeEntities.computeIfAbsent(caller + "$" + typeElement.getSimpleName() + "$WireBridge", name -> save(new WireBridgeEntity(typeElement.asType(), name, wireRepositories), entity -> attach(entity, typeElement).setPackageOf(typeElement)));
		}
	}

	public AspectAwareProxyEntity newAspectAwareProxy(TypeElement typeElement) {
		return save(new AspectAwareProxyEntity(typeElement), it -> attach(it, typeElement).setPackageOf(typeElement));
	}

	private AbstractClassEntity attach(AbstractClassEntity entity, Element typeElement) {
		return entity.setPackage(TypeUtils.packageOf(typeElement))
				.addSource(typeElement);
	}

	private IdentifiableProviderEntity create(TypeElement typeElement) {
		return new IdentifiableProviderEntity(typeElement, typeIdentifiers, wireRepositories, this);
	}

	private IdentifiableProviderEntity create(TypeMirror mirror, String name) {
		return new IdentifiableProviderEntity(mirror, name, typeIdentifiers, wireRepositories, this);
	}
}
