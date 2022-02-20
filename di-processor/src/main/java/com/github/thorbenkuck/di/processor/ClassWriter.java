package com.github.thorbenkuck.di.processor;

import com.github.thorbenkuck.di.processor.foundation.ProcessingException;
import com.github.thorbenkuck.di.processor.foundation.ProcessorContext;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import java.io.IOException;

public class ClassWriter {

	public static void write(TypeSpec typeSpec, Element rootElement) {
		PackageElement packageElement = ProcessorContext.getElements().getPackageOf(rootElement);

		try {
			JavaFile.builder(packageElement.getQualifiedName().toString(), typeSpec)
					.indent("    ")
					.build()
					.writeTo(ProcessorContext.getFiler());
		} catch (IOException e) {
			throw new ProcessingException(rootElement, e.getMessage());
		}
	}

}
