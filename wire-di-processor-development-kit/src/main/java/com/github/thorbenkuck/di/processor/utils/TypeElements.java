package com.github.thorbenkuck.di.processor.utils;

import com.github.thorbenkuck.di.processor.exceptions.ProcessingException;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

public class TypeElements {

	@NotNull
	public static <T> Class<T> asClass(TypeElement typeElement) {
		try {
			return (Class<T>) Class.forName(getClassName(typeElement));
		} catch (Exception e) {
			throw new ProcessingException(typeElement, "Could not instantiate class. Make sure that this class has no or one constructor without parameters!");
		}
	}

	@NotNull
	public static String getClassName(TypeElement element) {
		Element currElement = element;
		StringBuilder result = new StringBuilder(element.getSimpleName().toString());
		while (currElement.getEnclosingElement() != null) {
			currElement = currElement.getEnclosingElement();
			if (currElement instanceof TypeElement) {
				result.insert(0, currElement.getSimpleName() + "$");
			} else if (currElement instanceof PackageElement) {
				if (!currElement.getSimpleName().contentEquals("")) {
					result.insert(0, ((PackageElement) currElement)
							.getQualifiedName() + ".");
				}
			}
		}
		return result.toString();
	}
}
