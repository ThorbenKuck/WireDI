package com.wiredi.compiler.domain.entities;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

public class TypeSpecs {

    public CodeBlock parametersAsClassArrayInstance(ExecutableElement executableElement) {
        CodeBlock.Builder result = CodeBlock.builder();
        result.add("new $T { ", TypeName.get(Class[].class));
        boolean first = true;
        for (VariableElement parameter : executableElement.getParameters()) {
            if(first) {
                result.add("$T.class", ClassName.get(parameter.asType()));
                first = false;
            } else {
                result.add(", $T.class", ClassName.get(parameter.asType()));
            }
        }
        return result.add(" }").build();
    }
}
