package com.wiredi.compiler.domain.entities;

import com.squareup.javapoet.FieldSpec;
import com.wiredi.compiler.domain.AbstractClassEntity;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface FieldFactory {

    void append(FieldSpec.Builder builder, AbstractClassEntity<?> entity);

    static FieldFactory wrap(Consumer<FieldSpec.Builder> consumer) {
        return ((builder, entity) -> consumer.accept(builder));
    }

    static FieldFactory wrap(BiConsumer<FieldSpec.Builder, AbstractClassEntity<?>> consumer) {
        return (consumer::accept);
    }
}
