package com.github.thorbenkuck.di.processor.constructors;

import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.constructors.factory.CreateInstanceForPropertySourceMethodConstructor;
import com.github.thorbenkuck.di.processor.constructors.factory.CreateInstanceForWireMethodConstructor;
import com.squareup.javapoet.*;

import javax.lang.model.element.*;

public interface MethodConstructor {

    String methodName();

    void construct(WireInformation wireInformation, TypeSpec.Builder typeBuilder);

    default MethodSpec.Builder privateMethod() {
        return MethodSpec.methodBuilder(methodName())
                .addModifiers(Modifier.PRIVATE);
    }

    default MethodSpec.Builder publicMethod() {
        return MethodSpec.methodBuilder(methodName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    }

    default MethodSpec.Builder overwriteMethod() {
        return publicMethod().addAnnotation(Override.class);
    }

    static MethodConstructor createInstanceForWire() {
        return new CreateInstanceForWireMethodConstructor();
    }

    static MethodConstructor createInstanceForPropertySource() {
        return new CreateInstanceForPropertySourceMethodConstructor();
    }
}
