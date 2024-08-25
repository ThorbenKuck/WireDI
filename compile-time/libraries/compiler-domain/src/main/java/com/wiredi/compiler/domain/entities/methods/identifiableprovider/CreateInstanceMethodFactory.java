package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.WireRepositories;
import com.wiredi.compiler.domain.entities.WireBridgeEntity;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;
import com.wiredi.compiler.domain.injection.FieldInjectionPoint;
import com.wiredi.compiler.domain.injection.MethodInjectionPoint;
import com.wiredi.compiler.domain.injection.PostConstructInjectionPoint;
import com.wiredi.compiler.domain.injection.VariableContext;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.runtime.async.AsyncLoader;
import com.wiredi.runtime.lang.ReflectionsHelper;
import com.wiredi.runtime.values.FutureValue;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

public abstract class CreateInstanceMethodFactory implements StandaloneMethodFactory {

	private final Logger logger = Logger.get(getClass());
	private static final CodeBlock EMPTY = CodeBlock.builder().build();

	private final CompilerRepository compilerRepository;
	private final WireRepositories wireRepositories;

	protected CreateInstanceMethodFactory(CompilerRepository compilerRepository, WireRepositories wireRepositories) {
		this.compilerRepository = compilerRepository;
		this.wireRepositories = wireRepositories;
	}

	protected CodeBlock fieldInjectionStep(
			List<? extends FieldInjectionPoint> fieldInjections,
			ClassEntity entity,
			VariableContext variableContext
	) {
		if (fieldInjections.isEmpty()) {
			return EMPTY;
		}

		CodeBlock.Builder codeBlockBuilder = CodeBlock.builder().add("\n// Field injections\n");

		fieldInjections.forEach(injectionPoint -> {
			if (injectionPoint.isPackagePrivate() && !entity.willHaveTheSamePackageAs(injectionPoint.field())) {
				WireBridgeEntity bridge = compilerRepository.newWireBridgeEntity(entity.className(), injectionPoint.getDeclaringClass());
				codeBlockBuilder.add("$T", bridge.className())
						.add(".$L", bridge.bridgePackagePrivateField(injectionPoint));
				codeBlockBuilder.addStatement("(wireRepository, instance)");
			} else {
				String getValue = variableContext.instantiateVariableIfRequired(injectionPoint.field(), wireRepositories, codeBlockBuilder);

				if (injectionPoint.requiresReflection() || entity.requiresReflectionFor(injectionPoint.field())) {
					logger.reflectionWarning(injectionPoint.field());
//					codeBlockBuilder.add("// This Field requires reflections. If you are reading this think about make this field package private or protected instead\n");
					codeBlockBuilder.add("$T.setField(", ReflectionsHelper.class)
							.add("$S, ", injectionPoint.name())
							.add("$L, ", "instance")
							.add("$T.class, ", injectionPoint.getDeclaringClass())
							.addStatement("$L)", getValue);
				} else {
					codeBlockBuilder.addStatement("$L.$L = $L", "instance", injectionPoint.name(), getValue);
				}
			}
		});
		return codeBlockBuilder.build();
	}
	protected CodeBlock methodInjectionStep(List<? extends MethodInjectionPoint> methodInjections, ClassEntity<?> entity, VariableContext variableContext) {
		if (methodInjections.isEmpty()) {
			return EMPTY;
		}

		CodeBlock.Builder codeBlockBuilder = CodeBlock.builder().add("\n// Method Injections\n");
		methodInjections.forEach(injectionPoint -> codeBlockBuilder.add(callMethod(injectionPoint, entity, variableContext)));
		return codeBlockBuilder.build();
	}

	protected CodeBlock postConstruct(List<? extends PostConstructInjectionPoint> injectionPoints, ClassEntity<?> entity, VariableContext variableContext) {
		if (injectionPoints.isEmpty()) {
			return EMPTY;
		}

		CodeBlock.Builder codeBlockBuilder = CodeBlock.builder().add("\n// Now let us inform the class about its construction\n");
		injectionPoints.forEach(injectionPoint -> {
			if (injectionPoint.supportsAsyncInvocation()) {
				codeBlockBuilder.add("$T.run(() -> {\n", ParameterizedTypeName.get(AsyncLoader.class)).indent();
			}
			codeBlockBuilder.add(callMethod(injectionPoint, entity, variableContext));
			if (injectionPoint.supportsAsyncInvocation()) {
				codeBlockBuilder.unindent().add("});\n");
			}
		});


		// TODO Add PostConstruct method invocation
		return codeBlockBuilder.build();
	}

	protected CodeBlock callMethod(MethodInjectionPoint injectionPoint, ClassEntity<?> entity, VariableContext variableContext) {
		CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
		if (injectionPoint.isPackagePrivate() && !entity.willHaveTheSamePackageAs(injectionPoint.method())) {
			WireBridgeEntity bridge = compilerRepository.newWireBridgeEntity(entity.className(), injectionPoint.getDeclaringClass());
			codeBlockBuilder.add("$T", bridge.className())
					.add(".$L", bridge.bridgePackagePrivateMethod(injectionPoint));
			codeBlockBuilder.addStatement("(wireRepository, instance)");
		} else {
			String fetchVariables = getVariablesFromWireRepository(codeBlockBuilder, injectionPoint.parameters(), variableContext);

			if (injectionPoint.requiresReflection() || entity.requiresReflectionFor(injectionPoint.method())) {
				logger.reflectionWarning(injectionPoint.method());
//				codeBlockBuilder.add("// This function requires reflections. If you are reading this think about make this function package private or protected instead\n");
				codeBlockBuilder.add("$T.invokeMethod(", ReflectionsHelper.class)
						.add("$L, ", "instance")
						.add("$T.class, ", injectionPoint.getDeclaringClass())
						.add("$S, ", injectionPoint.name())
						.add("$T.class", injectionPoint.returnValue());
				if (!fetchVariables.isBlank()) {
					codeBlockBuilder.add(", $L", fetchVariables);
				}
				codeBlockBuilder.addStatement(")");
			} else {
				codeBlockBuilder.addStatement("$L.$L($L)", "instance", injectionPoint.name(), fetchVariables);
			}
		}
		return codeBlockBuilder.build();
	}

	protected String getVariablesFromWireRepository(CodeBlock.Builder rootCodeBlock, List<? extends VariableElement> parameters, VariableContext variableContext) {
		List<String> fetchVariables = new ArrayList<>();

		parameters.forEach(parameter -> {
			String variableName = variableContext.instantiateVariableIfRequired(parameter, wireRepositories, rootCodeBlock);
			fetchVariables.add(variableName);
		});

		return String.join(", ", fetchVariables);
	}

	protected Elements elements() {
		return this.compilerRepository.getElements();
	}

	public Types types() {
		return this.compilerRepository.getTypes();
	}

	public TypeName classNameOf(TypeMirror typeMirror) {
		return ClassName.get(types().erasure(typeMirror));
	}

	public TypeName classNameOf(Element element) {
		return classNameOf(element.asType());
	}

	@Override
	public String methodName() {
		return "createInstance";
	}
}
