package com.github.thorbenkuck.di.processor;

import com.github.thorbenkuck.di.DataAccess;
import com.github.thorbenkuck.di.processor.exceptions.ProcessingException;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

public class ClassWriter {

	private static final Queue<WritingInformation> queue = new ArrayDeque<>();
	private static final DataAccess dataAccess = new DataAccess();

	static Queue<WritingInformation> drain() {
		return dataAccess.read(() -> {
			ArrayDeque<WritingInformation> writingInformation = new ArrayDeque<>(queue);
			queue.clear();

			return writingInformation;
		});
	}

	public static void append(TypeSpec typeSpec, Element rootElement) {
		dataAccess.write(() -> queue.add(new WritingInformation(typeSpec, rootElement)));
	}

	public static class WritingInformation {
		private final TypeSpec typeSpec;
		private final Element rootElement;

		public WritingInformation(TypeSpec typeSpec, Element rootElement) {
			this.typeSpec = typeSpec;
			this.rootElement = rootElement;
		}

		public TypeSpec getTypeSpec() {
			return typeSpec;
		}

		public Element getRootElement() {
			return rootElement;
		}
	}
}
