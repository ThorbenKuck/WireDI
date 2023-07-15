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
import java.util.*;
import java.util.function.Consumer;

public class CompilerRepository {

	private static final Logger logger = Logger.get(CompilerRepository.class);

	private final Set<ClassEntity> classEntries = new HashSet<>();

	private final Map<String, WireBridgeEntity> wireBridgeEntities = new HashMap<>();

	@Inject
	private Filer filer;
	@Inject
	private Elements elements;
	@Inject
	private Types types;
	@Inject
	private WireRepositories wireRepositories;

	public void flush() {
		synchronized (classEntries) {
			classEntries.forEach(entry -> {
				TypeSpec typeSpec = entry.build();
				var rootType = types.asElement(entry.rootType());
				PackageElement packageElement = entry.packageElement().orElseGet(() -> elements.getPackageOf(rootType));

				logger.info("Writing the file " + entry.className());
				try {
					JavaFile.builder(packageElement.getQualifiedName().toString(), typeSpec)
							.indent("   ")
							.build()
							.writeTo(filer);
				} catch (Exception e) {
					throw new ProcessingException(rootType, "Failed to write the java file " + entry.className(), e);
				}
			});
			classEntries.clear();
		}
	}

	public <T extends ClassEntity> T save(T entity, Consumer<T> consumer) {
		synchronized (classEntries) {
			this.classEntries.add(entity);
			consumer.accept(entity);
		}
		return entity;
	}

	public EnvironmentConfigurationEntity newEnvironmentConfiguration(TypeElement typeElement) {
		return save(new EnvironmentConfigurationEntity(typeElement), entity -> attach(entity, typeElement));
	}

	public AspectHandlerEntity newAspectHandlerInstance(TypeElement typeElement, ExecutableElement executableElement) {
		return save(new AspectHandlerEntity(executableElement), entity -> attach(entity, typeElement).addSource(executableElement));
	}

	public IdentifiableProviderEntity newIdentifiableProvider(String name, TypeMirror typeMirror) {
		Element element = types.asElement(typeMirror);
		return save(create(typeMirror, name), entity -> attach(entity, element));
	}

	public IdentifiableProviderEntity newIdentifiableProvider(TypeElement typeElement) {
		return save(create(typeElement), entity -> attach(entity, typeElement).setPackageOf(typeElement));
	}

	public WireBridgeEntity newWireBridgeEntity(String caller, TypeElement typeElement) {
		synchronized (wireBridgeEntities) {
			return wireBridgeEntities.computeIfAbsent(caller + "$" + typeElement.getSimpleName() + "$WireBridge", name -> save(new WireBridgeEntity(typeElement.asType(), name, wireRepositories), entity -> attach(entity, typeElement).setPackageOf(typeElement)));
		}
	}

	public AspectAwareProxyEntity newAspectAwareProxy(TypeElement typeElement) {
		return save(new AspectAwareProxyEntity(typeElement), it -> attach(it, typeElement).setPackageOf(typeElement));
	}

	private <T extends AbstractClassEntity<T>> T attach(T entity, Element typeElement) {
		return entity.setPackage(TypeUtils.packageOf(typeElement))
				.addSource(typeElement);
	}

	private IdentifiableProviderEntity create(TypeElement typeElement) {
		return new IdentifiableProviderEntity(typeElement);
	}

	private IdentifiableProviderEntity create(TypeMirror mirror, String name) {
		return new IdentifiableProviderEntity(mirror, name);
	}
}
