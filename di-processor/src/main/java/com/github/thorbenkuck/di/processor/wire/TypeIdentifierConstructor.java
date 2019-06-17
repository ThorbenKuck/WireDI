package com.github.thorbenkuck.di.processor.wire;

import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.processor.foundation.ProcessingException;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;

public class TypeIdentifierConstructor {

	private final TypeElement typeElement;
	private final Types types;

	public TypeIdentifierConstructor(TypeElement typeElement, Types types) {
		this.typeElement = typeElement;
		this.types = types;
	}

	public void analyze(TypeSpec.Builder builder) {
		builder.addMethod(MethodSpec.methodBuilder("type")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.returns(ClassName.get(Class.class))
				.addCode(CodeBlock.builder().addStatement("return $T.class", ClassName.get(typeElement)).build())
				.build());

		MethodSpec.Builder wiredTypesBuilder = MethodSpec.methodBuilder("wiredTypes")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.returns(TypeName.get(Class[].class));

		Wire annotation = typeElement.getAnnotation(Wire.class);

		try {
			annotation.to();
		} catch (javax.lang.model.type.MirroredTypesException e) {
			List<? extends TypeMirror> typeMirrors = e.getTypeMirrors();

			for(TypeMirror typeMirror : typeMirrors) {
				if(!types.isAssignable(typeElement.asType(), typeMirror)) {
					throw new ProcessingException(typeElement, "The annotated element " + typeElement + " is not assignable to " + typeMirror);
				}
			}

			if (typeMirrors.isEmpty()) {
				wiredTypesBuilder.addCode(CodeBlock.builder()
						.addStatement("return new Class[]{$T.class}", ClassName.get(typeElement))
						.build());
			} else {
				CodeBlock.Builder add = CodeBlock.builder()
						.add("new Class[]{")
						.add("$T.class", ClassName.get(typeMirrors.get(0)));

				for (int i = 1; i < typeMirrors.size(); i++) {
					add.add(", ").add("$T.class", ClassName.get(typeMirrors.get(i)));
				}

				add.add("}");

				builder.addField(FieldSpec.builder(Class[].class, "types")
						.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
						.initializer(add.build())
						.build());

				wiredTypesBuilder.addCode(CodeBlock.builder()
						.addStatement("return types")
						.build());
			}
		}


		builder.addMethod(wiredTypesBuilder.build());
	}
}
