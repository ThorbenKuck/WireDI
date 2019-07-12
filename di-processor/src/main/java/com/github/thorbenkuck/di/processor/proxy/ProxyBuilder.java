package com.github.thorbenkuck.di.processor.proxy;

import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.processor.util.MethodOverrider;
import com.github.thorbenkuck.di.processor.foundation.Logger;
import com.github.thorbenkuck.di.processor.foundation.ProcessingException;
import com.squareup.javapoet.*;

import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProxyBuilder {

	private final Logger logger;
	private final List<MethodSpec> methods = new ArrayList<>();
	private TypeElement toProxy;
	private Wire wire;
	private String name;

	public ProxyBuilder(Logger logger) {
		this.logger = logger;
	}

	private void delegate(ExecutableElement method) {
		MethodSpec build = MethodOverrider.delegateMethod(method, CodeBlock.builder()
				.addStatement("$T.out.println($S)", System.class, "[PROXY]: [" + method.getSimpleName() + "]: Start")
				.build())
				.addStatement("$T.out.println($S)", System.class, "[PROXY]: [" + method.getSimpleName() + "]: End")
				.build();

		methods.add(build);
	}

	public ProxyBuilder basedOnTypeElement(TypeElement typeElement) {
		this.toProxy = typeElement;
		name = toProxy.getSimpleName() + "Proxy";

		if (typeElement.getAnnotation(Wire.class) != null) {
			throw new ProcessingException(typeElement, "The use of @Wire is not permitted on classes annotated with @Proxy.\n" +
					"Please instead use the wire method of the @Proxy annotation");
		}

		if (typeElement.getModifiers().contains(Modifier.PRIVATE) || typeElement.getModifiers().contains(Modifier.FINAL)) {
			throw new ProcessingException(typeElement, "Only classes without private or final modifications may be proxied!");
		}

		return this;
	}

	public ProxyBuilder wireWith(Wire wire) {
		this.wire = wire;

		return this;
	}

	public ProxyBuilder withName(String name) {
		if (Objects.isNull(name)) {
			throw new ProcessingException(toProxy, "Null is not allowed as a name for a proxy!");
		}
		if (!name.isEmpty()) {
			this.name = name;
		}
		if (name.equals(this.name)) {
			throw new ProcessingException(toProxy, "The given proxy name may not equal the class name!");
		}

		return this;
	}

	public ProxyBuilder overridePublicMethods() {
		for (Element enclosedElement : toProxy.getEnclosedElements()) {
			if (enclosedElement.getKind() == ElementKind.METHOD &&
					enclosedElement.getModifiers().contains(Modifier.PUBLIC)) {
				ExecutableElement method = (ExecutableElement) enclosedElement;
				delegate(method);
			}
		}

		return this;
	}

	public ProxyBuilder overrideProtectedMethods() {
		for (Element enclosedElement : toProxy.getEnclosedElements()) {
			if (enclosedElement.getKind() == ElementKind.METHOD &&
					enclosedElement.getModifiers().contains(Modifier.PROTECTED)) {
				ExecutableElement method = (ExecutableElement) enclosedElement;
				delegate(method);
			}
		}

		return this;
	}

	public ProxyBuilder overridePackagePrivateMethods() {
		for (Element enclosedElement : toProxy.getEnclosedElements()) {
			if (enclosedElement.getKind() == ElementKind.METHOD &&
					!enclosedElement.getModifiers().contains(Modifier.PUBLIC) &&
					!enclosedElement.getModifiers().contains(Modifier.PRIVATE) &&
					!enclosedElement.getModifiers().contains(Modifier.PROTECTED)) {
				ExecutableElement method = (ExecutableElement) enclosedElement;
				delegate(method);
			}
		}

		return this;
	}

	public ProxyBuilder overrideConstructors() {
		for (Element enclosedElement : toProxy.getEnclosedElements()) {
			if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR &&
					!enclosedElement.getModifiers().contains(Modifier.PRIVATE)) {
				ExecutableElement method = (ExecutableElement) enclosedElement;
				MethodSpec build = MethodOverrider.delegateConstructor(method)
						.addStatement("$T.out.println($S)", System.class, "[PROXY]: Constructed")
						.build();

				methods.add(build);
			}
		}

		return this;
	}

	public TypeSpec.Builder builder() {
		List<TypeName> typeNames = new ArrayList<>();
		StringBuilder stringBuilder = new StringBuilder("{");

		try {
			wire.to();
			typeNames.add(TypeName.get(toProxy.asType()));
		} catch (MirroredTypesException e) {
			List<TypeMirror> typeMirrors = new ArrayList<>(e.getTypeMirrors());
			if(typeMirrors.isEmpty()) {
				typeMirrors.add(toProxy.asType());
			}
			for(TypeMirror typeMirror : typeMirrors) {
				typeNames.add(TypeName.get(typeMirror));
			}
		}

		stringBuilder.append("$T.class");

		for(int i = 1 ; i < typeNames.size() ; i++) {
			stringBuilder.append(", ").append("$T.class");
		}

		AnnotationSpec build = AnnotationSpec.builder(Wire.class)
				.addMember("to", stringBuilder.append("}").toString(), (Object[]) typeNames.toArray())
				.addMember("lazy", "$L", wire.lazy())
				.build();


		TypeSpec.Builder superclass = TypeSpec.classBuilder(name)
				.superclass(TypeName.get(toProxy.asType()))
				.addAnnotation(build);

		for (MethodSpec methodSpec : methods) {
			superclass.addMethod(methodSpec);
		}

		return superclass;
	}
}
