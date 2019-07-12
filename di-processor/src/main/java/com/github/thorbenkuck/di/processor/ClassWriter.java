package com.github.thorbenkuck.di.processor;

import com.github.thorbenkuck.di.processor.foundation.ProcessingException;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import java.io.IOException;

public class ClassWriter {

	public static Filer filer;
	public static Elements elements;

	public static void write(TypeSpec typeSpec, Element rootElement) {
		PackageElement packageElement = elements.getPackageOf(rootElement);

		try {
			JavaFile.builder(packageElement.getQualifiedName().toString(), typeSpec)
					.indent("    ")
					.build()
					.writeTo(filer);
		} catch (IOException e) {
			throw new ProcessingException(rootElement, e.getMessage());
		}
	}

}
