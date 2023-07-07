package com.wiredi.compiler.domain.injection;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public class PostConstructInjectionPoint extends MethodInjectionPoint {

	public PostConstructInjectionPoint(ExecutableElement method, boolean supportsAsyncInvocation) {
		super(method, supportsAsyncInvocation);
	}
}
