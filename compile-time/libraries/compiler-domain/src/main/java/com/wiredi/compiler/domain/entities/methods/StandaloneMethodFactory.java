package com.wiredi.compiler.domain.entities.methods;

import com.squareup.javapoet.MethodSpec;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.ClassEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface StandaloneMethodFactory extends MethodFactory {

    @NotNull
    String methodName();

    default boolean applies(@NotNull ClassEntity<?> entity) {
        return true;
    }

    @NotNull
    static StandaloneMethodFactory wrap(@NotNull String name, @NotNull Consumer<MethodSpec.Builder> methodFactory) {
        return new StandaloneMethodFactory() {
            @Override
            public @NotNull String methodName() {
                return name;
            }

            @Override
            public void append(MethodSpec.@NotNull Builder builder, @NotNull ClassEntity<?> entity) {
                methodFactory.accept(builder);
            }
        };
    }

    static StandaloneMethodFactory wrap(String name, MethodFactory methodFactory) {

        return new StandaloneMethodFactory() {
            @Override
            public @NotNull String methodName() {
                return name;
            }

            @Override
            public void append(MethodSpec.@NotNull Builder builder, @NotNull ClassEntity<?> entity) {
                methodFactory.append(builder, entity);
            }
        };
    }

}
