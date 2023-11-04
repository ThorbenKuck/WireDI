package com.wiredi.compiler.repository;

import com.wiredi.compiler.domain.ClassEntity;

public interface CompilerRepositoryCallback {

    default void finalize(ClassEntity<?> classEntity) {}

    default void saved(ClassEntity<?> classEntity) {}

}
