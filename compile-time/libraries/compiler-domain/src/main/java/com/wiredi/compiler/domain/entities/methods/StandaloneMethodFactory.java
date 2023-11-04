package com.wiredi.compiler.domain.entities.methods;

import com.squareup.javapoet.MethodSpec;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.ClassEntity;

import java.util.function.Consumer;

public interface StandaloneMethodFactory extends MethodFactory {

    String methodName();

    default boolean applies(ClassEntity<?> entity) {
        return true;
    }

    static StandaloneMethodFactory wrap(String name, Consumer<MethodSpec.Builder> methodFactory) {
        return new StandaloneMethodFactory() {
            @Override
            public String methodName() {
                return name;
            }

            @Override
            public void append(MethodSpec.Builder builder, ClassEntity<?> entity) {
                methodFactory.accept(builder);
            }
        };
    }

    static StandaloneMethodFactory wrap(String name, MethodFactory methodFactory) {

        return new StandaloneMethodFactory() {
            @Override
            public String methodName() {
                return name;
            }

            @Override
            public void append(MethodSpec.Builder builder, ClassEntity<?> entity) {
                methodFactory.append(builder, entity);
            }
        };
    }

}
