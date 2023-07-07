package com.wiredi.compiler.domain.injection;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;

public record FieldInjectionPoint(
		VariableElement field
) implements InjectionPoint {

	public TypeElement getDeclaringClass() {
		Element encloser = field.getEnclosingElement();
		while (!(encloser instanceof TypeElement)) {
			if (encloser instanceof PackageElement) {
				throw new IllegalStateException("Could not determine the declaring class!");
			}
			encloser = field.getEnclosingElement();
		}

		return (TypeElement) encloser;
	}

	public boolean requiresReflection() {
		if (field.getModifiers().contains(Modifier.PRIVATE)) {
			return true;
		}

		return false;
	}

	public boolean isPackagePrivate() {
		return !field.getModifiers().contains(Modifier.PRIVATE) && !field.getModifiers().contains(Modifier.PUBLIC);
	}

	public TypeMirror type() {
		return field.asType();
	}

	public String name() {
		return field.getSimpleName().toString();
	}
}
