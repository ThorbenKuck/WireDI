package com.wiredi.compiler.domain;

import com.squareup.javapoet.CodeBlock;
import com.wiredi.compiler.errors.ProcessingException;
import com.wiredi.compiler.logger.LogLevel;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.runtime.qualifier.QualifierType;
import jakarta.inject.Qualifier;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.*;

public class Qualifiers {

	private static final Logger logger = Logger.get(Qualifiers.class);

	public static List<QualifierType> allQualifiersOf(Element element) {
		List<QualifierType> result = new ArrayList<>();

		element.getAnnotationMirrors()
				.stream()
				.map(Qualifiers::allQualifiersOf)
				.forEach(result::addAll);

		if (logger.isEnabled(LogLevel.DEBUG)) {
			result.forEach(qualifier -> logger.debug(element, "Found qualifier " + qualifier));
		}

		return result;
	}

	@Nullable
	public static QualifierType injectionQualifier(Element element) {
		List<QualifierType> qualifierTypes = allQualifiersOf(element);
		if (qualifierTypes.size() > 1) {
			throw new ProcessingException(element, "Only one injection qualifier is allowed at injection points");
		}
		if (qualifierTypes.isEmpty()) {
			return null;
		}
		return qualifierTypes.get(0);
	}

	public static List<QualifierType> allQualifiersOf(AnnotationMirror annotationMirror) {
		List<QualifierType> result = new ArrayList<>();
		QualifierType qualifier = asQualifier(annotationMirror);
		if (qualifier != null) {
			result.add(qualifier);
		}

		annotationMirror.getAnnotationType()
				.getAnnotationMirrors()
				.stream()
				.filter(Annotations::isNotJdkAnnotation)
				.map(Qualifiers::allQualifiersOf)
				.forEach(result::addAll);

		return result;
	}

	public static @Nullable QualifierType asQualifier(AnnotationMirror annotationMirror) {
		if (Annotations.isAnnotatedWith(annotationMirror, Qualifier.class)) {
			return QualifierType.builder(annotationMirror.getAnnotationType().asElement().toString())
					.addAll(values(annotationMirror))
					.build();
		}

		return null;
	}

	private static Map<String, Object> values(AnnotationMirror annotationMirror) {
		HashMap<String, Object> fields = new HashMap<>();

		annotationMirror.getAnnotationType().asElement().getEnclosedElements().stream().map(it -> (ExecutableElement) it)
				.forEach(method -> Optional.ofNullable(method.getDefaultValue()).ifPresent(defaultValue -> fields.put(method.getSimpleName().toString(), defaultValue.getValue())));
		annotationMirror.getElementValues().forEach((key, value) -> fields.put(key.getSimpleName().toString(), value.getValue().toString()));

		return fields;
	}

	public static CodeBlock qualifierValueBuilder(QualifierType qualifier) {
		if (qualifier.values().isEmpty()) {
			return CodeBlock.builder().add("$T.just($S)", QualifierType.class, qualifier.name()).build();
		} else {
			CodeBlock.Builder builder = CodeBlock.builder()
					.add("$T.builder($S)", QualifierType.class, qualifier.name())
					.indent();
			qualifier.forEach((key, value) -> {
				builder.add("\n.add($S, ", key);
				if (value instanceof String) {
					builder.add("$S", value);
				} else if (value instanceof Character) {
					builder.add("'$L'", value);
				} else if (value instanceof Class<?> c) {
					builder.add("$S", c.getName());
				} else {
					builder.add("$L", value);
				}
				builder.add(")");
			});
			return builder.add("\n.build()").unindent().build();
		}
	}
}
