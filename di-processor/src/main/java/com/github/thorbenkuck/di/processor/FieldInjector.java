package com.github.thorbenkuck.di.processor;

import com.github.thorbenkuck.di.DiInstantiationException;
import com.github.thorbenkuck.di.ReflectionsHelper;
import com.github.thorbenkuck.di.processor.foundation.ProcessingException;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.List;

public class FieldInjector {

	public static CodeBlock createCode(List<VariableElement> fields) {
		CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
		if (fields.isEmpty()) {
			return codeBlockBuilder.build();
		}
		int count = 0;
		for (VariableElement field : fields) {
			codeBlockBuilder.addStatement("$T t$L = wiredTypes.getInstance($T.class)", ClassName.get(field.asType()), count, ClassName.get(field.asType()));

			if (field.getModifiers().contains(Modifier.FINAL)) {
				throw new ProcessingException(field, "Cannot inject into a final field!");
			}

			if (field.getAnnotation(Nullable.class) == null) {
				codeBlockBuilder.beginControlFlow("if(t$L == null)", count)
						.addStatement("throw new $T($S)", DiInstantiationException.class, "Could not find a non nullable instance for the type: " + field.asType().toString())
						.endControlFlow();
			}

			if (field.getModifiers().contains(Modifier.PRIVATE)) {
				// TODO Inform that the use of private and Inject is bad
				codeBlockBuilder.addStatement("$T.setField($S, instance, t$L)", ReflectionsHelper.class, field.getSimpleName(), count);
			} else {
				codeBlockBuilder.addStatement("instance.$L = t$L", field.getSimpleName(), count);
			}

			++count;
		}

		return codeBlockBuilder.build();
	}

}
