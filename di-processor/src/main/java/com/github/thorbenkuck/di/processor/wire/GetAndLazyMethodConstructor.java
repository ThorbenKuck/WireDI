package com.github.thorbenkuck.di.processor.wire;

import com.github.thorbenkuck.di.DiInstantiationException;
import com.github.thorbenkuck.di.ReflectionsHelper;
import com.github.thorbenkuck.di.Repository;
import com.github.thorbenkuck.di.annotations.Nullable;
import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.processor.ProcessingException;
import com.squareup.javapoet.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;

public class GetAndLazyMethodConstructor {

	private MethodSpec.Builder lazyMethod;
	private MethodSpec.Builder getMethod;
	private boolean lazy;
	private final TypeElement typeElement;

	public GetAndLazyMethodConstructor(TypeElement typeElement) {
		this.typeElement = typeElement;
	}

	public void setLazyMethod(boolean to) {
		lazy = to;
		lazyMethod = MethodSpec.methodBuilder("lazy")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.returns(TypeName.BOOLEAN)
				.addCode(CodeBlock.builder().addStatement("return $L", to).build());
	}

	private List<VariableElement> getAnnotatedFields() {
		final List<VariableElement> variableElements = new ArrayList<>();
		for (Element enclosedElement : typeElement.getEnclosedElements()) {
			if(enclosedElement.getKind() == ElementKind.FIELD && enclosedElement.getAnnotation(Inject.class) != null) {
				variableElements.add((VariableElement) enclosedElement);
			}
		}

		return variableElements;
	}

	public void analyze(TypeSpec.Builder typeBuilder) {
		setLazyMethod(typeElement.getAnnotation(Wire.class).lazy());

		typeBuilder.addField(FieldSpec.builder(TypeName.get(typeElement.asType()), "instance")
				.addModifiers(Modifier.PRIVATE)
				.build());

		typeBuilder.addMethod(MethodSpec.methodBuilder("get")
				.addAnnotation(Override.class)
				.returns(TypeName.get(typeElement.asType()))
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addCode("return instance;\n")
				.build());

		CodeBlock.Builder aReturn = CodeBlock.builder();

		if(typeElement.getAnnotation(Singleton.class) != null) {
			aReturn.beginControlFlow("if(instance != null)")
					.addStatement("return")
					.endControlFlow();
		}

		aReturn.addStatement("instance = $L(wiredTypes)", ConstructorFinder.INSTANTIATION_METHOD_NAME);
		appendFieldInjections(aReturn);

		typeBuilder.addMethod(MethodSpec.methodBuilder("instantiate")
				.addAnnotation(Override.class)
				.addParameter(Repository.class, "wiredTypes")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addCode(aReturn.build())
				.build());


		typeBuilder.addMethod(lazyMethod.build());
	}

	private void appendFieldInjections(CodeBlock.Builder aReturn) {
		List<VariableElement> toInjectFields = getAnnotatedFields();

		if(toInjectFields.isEmpty()) {
			return;
		}

		int count = 0;
		for(VariableElement field : toInjectFields) {
			aReturn.addStatement("$T t$L = wiredTypes.getInstance($T.class)", ClassName.get(field.asType()), count, ClassName.get(field.asType()));

			if(field.getModifiers().contains(Modifier.FINAL)) {
				throw new ProcessingException(field, "Cannot inject into a final field!");
			}

			if(field.getAnnotation(Nullable.class) == null) {
				aReturn.beginControlFlow("if(t$L == null)", count)
						.addStatement("throw new $T($S)", DiInstantiationException.class, "Could not find a non nullable instance for the type: " + field.asType().toString())
						.endControlFlow();
			}

			if(field.getModifiers().contains(Modifier.PRIVATE)) {
				// TODO Inform that the use of private and Inject is bad
				aReturn.addStatement("$T.setField($S, instance, t$L)", ReflectionsHelper.class, field.getSimpleName(), count);
			} else {
				aReturn.addStatement("instance.$L = t$L", field.getSimpleName(), count);
			}

			++count;
		}
	}
}
