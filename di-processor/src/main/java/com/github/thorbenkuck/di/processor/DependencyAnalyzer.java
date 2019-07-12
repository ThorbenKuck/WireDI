package com.github.thorbenkuck.di.processor;

import javax.lang.model.element.ExecutableElement;

public class DependencyAnalyzer {

	private final ExecutableElement executableElement;

	public DependencyAnalyzer(ExecutableElement executableElement) {
		this.executableElement = executableElement;
	}

	public DependencyAnalyzer calculate() {
		// TODO Find circular dependencies and display them.
		return this;
	}

	public void requireNoCircularDependencies() {

	}
}
