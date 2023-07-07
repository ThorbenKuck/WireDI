package com.wiredi.compiler.domain.injection;

import com.wiredi.compiler.domain.Qualifiers;
import com.wiredi.compiler.domain.TypeUtils;
import com.wiredi.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VariableContext {

	@NotNull
	private final NameContext nameContext = new NameContext();

	@NotNull
	private final String varPrefix;

	@NotNull
	private final Map<TypeMirror, Map<QualifierType, String>> qualifiedCache = new HashMap<>();

	@NotNull
	private final Map<TypeMirror, String> unqualifiedCache = new HashMap<>();

	public VariableContext(@NotNull String varPrefix) {
		this.varPrefix = varPrefix;
	}

	public VariableContext() {
		this("variable");
	}

	/**
	 * Allows for single instantiations of variables and therefore reusing existing variables.
	 * <p>
	 * The neNameFunction will be invoked, if a new variable name is created.
	 *
	 * @param typeMirror      the type of the variable
	 * @param newNameFunction the function to invoke if a new variable is created
	 * @return the name associated with the typeMirror
	 */
	public String instantiateVariableIfRequired(TypeMirror typeMirror, Consumer<String> newNameFunction) {
		if (TypeUtils.isSingleton(typeMirror)) {
			return unqualifiedCache.computeIfAbsent(typeMirror, (typeName) -> {
				String nextName = nameContext.nextName(varPrefix);
				newNameFunction.accept(nextName);
				return nextName;
			});
		} else {
			String nextName = nameContext.nextName(varPrefix);
			newNameFunction.accept(nextName);
			return nextName;
		}
	}
	public String instantiateVariableIfRequired(Element element, BiConsumer<String, @Nullable QualifierType> newNameFunction) {
		QualifierType qualifierType = Qualifiers.injectionQualifier(element);
		if (qualifierType != null) {
			return instantiateVariableIfRequired(qualifierType, element.asType(), newNameFunction);
		} else {
			return instantiateVariableIfRequired(element. asType(), (name) -> newNameFunction.accept(name, null));
		}
	}

	/**
	 * Allows for single instantiations of variables and therefore reusing existing variables.
	 * <p>
	 * The neNameFunction will be invoked, if a new variable name is created.
	 *
	 * @param typeMirror      the type of the variable
	 * @param newNameFunction the function to invoke if a new variable is created
	 * @return the name associated with the typeMirror
	 */
	public String instantiateVariableIfRequired(
			QualifierType qualifierType,
			TypeMirror typeMirror,
			BiConsumer<String, QualifierType> newNameFunction
	) {
		if (TypeUtils.isSingleton(typeMirror)) {
			Map<QualifierType, String> qualifierMap = qualifiedCache.computeIfAbsent(typeMirror, (t) -> new HashMap<>());
			return qualifierMap.computeIfAbsent(qualifierType, (typeName) -> {
				String nextName = nameContext.nextName(varPrefix);
				newNameFunction.accept(nextName, qualifierType);
				return nextName;
			});
		} else {
			String nextName = nameContext.nextName(varPrefix);
			newNameFunction.accept(nextName, qualifierType);
			return nextName;
		}
	}
}
