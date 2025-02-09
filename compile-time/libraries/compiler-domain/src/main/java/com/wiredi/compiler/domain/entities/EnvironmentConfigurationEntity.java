package com.wiredi.compiler.domain.entities;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.entities.environment.EnvironmentModification;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.resources.Resource;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import com.wiredi.runtime.resources.ResourceLoader;
import com.wiredi.runtime.resources.builtin.ClassPathResourceProtocolResolver;
import com.wiredi.runtime.resources.builtin.FileSystemResourceProtocolResolver;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnvironmentConfigurationEntity extends AbstractClassEntity<EnvironmentConfigurationEntity> {

	private final List<String> propertiesToLoad = new ArrayList<>();
	private final List<EnvironmentModification> entries = new ArrayList<>();
	private final ResourceLoader resourceLoader = ResourceLoader.open(ClassPathResourceProtocolResolver.INSTANCE, FileSystemResourceProtocolResolver.INSTANCE);
	private final TypeElement typeElement;
	private static final Logger logger = Logger.get(EnvironmentConfigurationEntity.class);

	public EnvironmentConfigurationEntity(TypeElement element) {
		super(element, element.asType(), element.getSimpleName().toString() + "EnvironmentConfiguration");
		this.typeElement = element;
	}

	@Override
	protected TypeSpec.Builder createBuilder(TypeMirror type) {
		return TypeSpec.classBuilder(className())
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addSuperinterface(EnvironmentConfiguration.class)
				.addAnnotation(
						AnnotationSpec.builder(AutoService.class)
								.addMember("value", "$T.class", EnvironmentConfiguration.class)
								.build()
				);
	}

	public EnvironmentConfigurationEntity appendSourceFiles(String[] paths) {
		this.propertiesToLoad.addAll(Arrays.asList(paths));
		for (String path : paths) {
			Resource resource = resourceLoader.load(path);
			if (!resource.exists()) {
				logger.warn(typeElement, () -> "The defined resource \"" + path + "\" appears to not exist");
			}
		}
		return this;
	}

	public EnvironmentConfigurationEntity appendModification(EnvironmentModification entry) {
		this.entries.add(entry);
		return this;
	}

	public EnvironmentConfigurationEntity appendModifications(List<EnvironmentModification> entries) {
		this.entries.addAll(entries);
		return this;
	}

	@Override
	protected void finalize(TypeSpec.Builder builder) {
		CodeBlock.Builder codeBlock = CodeBlock.builder();

		propertiesToLoad.forEach(property -> {
			codeBlock.addStatement("environment.loadResource($S).ifExists(environment::appendPropertiesFrom)", property);
		});

		entries.forEach(entry -> {
			entry.apply(codeBlock);
		});

		builder.addMethod(
				MethodSpec.methodBuilder("configure")
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.addAnnotation(Override.class)
						.addParameter(
								ParameterSpec.builder(Environment.class, "environment")
										.addModifiers(Modifier.FINAL)
										.addAnnotation(NotNull.class)
										.build()
						)
						.addCode(codeBlock.build())
						.build()
		);
	}
}
