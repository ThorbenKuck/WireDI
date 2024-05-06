package com.wiredi.compiler.processor.factories;

import com.wiredi.compiler.domain.ClassEntity;

import javax.lang.model.element.TypeElement;

public interface Factory<T extends ClassEntity> {

	T create(TypeElement typeElement);
}
