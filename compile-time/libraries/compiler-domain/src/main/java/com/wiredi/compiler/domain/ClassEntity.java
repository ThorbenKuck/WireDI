package com.wiredi.compiler.domain;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public interface ClassEntity {

	Optional<PackageElement> packageElement();

	ClassName className();

	TypeSpec build();

	TypeMirror rootType();

}
