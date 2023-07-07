package com.wiredi.compiler.domain.values;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public record ProxyMethod(ExecutableElement value, List<AnnotationMirror> proxyAnnotations) {

	public TypeMirror returnType() {
		return value.getReturnType();
	}

	public boolean willReturnSomething() {
		return returnType().getKind() != TypeKind.VOID;
	}

	public String simpleName() {
		return value.getSimpleName().toString();
	}

	public List<? extends VariableElement> parameters() {
		return value.getParameters();
	}
}
