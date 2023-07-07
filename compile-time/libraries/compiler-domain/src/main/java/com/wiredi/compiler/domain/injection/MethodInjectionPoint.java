package com.wiredi.compiler.domain.injection;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public class MethodInjectionPoint implements InjectionPoint {

	private final ExecutableElement method;
	private final boolean supportsAsyncInvocation;

	public MethodInjectionPoint(ExecutableElement method) {
		this.method = method;
		this.supportsAsyncInvocation = false;
	}

	public MethodInjectionPoint(ExecutableElement method, boolean supportsAsyncInvocation) {
		this.method = method;
		this.supportsAsyncInvocation = supportsAsyncInvocation;
	}

	public TypeElement getDeclaringClass() {
		Element encloser = method.getEnclosingElement();
		while (!(encloser instanceof TypeElement)) {
			if (encloser instanceof PackageElement) {
				throw new IllegalStateException("Could not determine the declaring class!");
			}

			encloser = method.getEnclosingElement();
		}
		return (TypeElement) encloser;
	}

	public boolean supportsAsyncInvocation() {
		return supportsAsyncInvocation;
	}

	public boolean requiresReflection() {
		if (method.getModifiers().contains(Modifier.PRIVATE)) {
			return true;
		}

		return false;
	}

	public ExecutableElement method() {
		return method;
	}

	public String name() {
		return method.getSimpleName().toString();
	}

	public List<? extends VariableElement> parameters() {
		return method.getParameters();
	}

	public TypeMirror returnValue() {
		return method.getReturnType();
	}

	public boolean isPackagePrivate() {
		return !method.getModifiers().contains(Modifier.PRIVATE) && !method.getModifiers().contains(Modifier.PUBLIC);
	}
}
