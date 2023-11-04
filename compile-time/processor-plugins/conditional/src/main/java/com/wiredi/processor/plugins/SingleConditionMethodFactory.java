package com.wiredi.processor.plugins;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.AnnotationMetaDataSpec;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;
import com.wiredi.domain.provider.condition.LoadCondition;
import com.wiredi.domain.provider.condition.SingleLoadCondition;
import com.wiredi.lang.values.Value;

import javax.lang.model.element.Modifier;

public class SingleConditionMethodFactory implements StandaloneMethodFactory {

    private static final String FIELD_NAME = "LOAD_CONDITION";
    private final ConditionEntry conditionEntry;

    public SingleConditionMethodFactory(ConditionEntry conditionEntry) {
        this.conditionEntry = conditionEntry;
    }

    @Override
    public void append(MethodSpec.Builder builder, ClassEntity<?> entity) {
        entity.addField(
                ParameterizedTypeName.get(Value.class, LoadCondition.class),
                FIELD_NAME,
                field -> field.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer(
                                CodeBlock.builder()
                                        .add("$T.async(() -> new $T(\n", Value.class, SingleLoadCondition.class)
                                        .indent()
                                        .add("$T.class,\n", conditionEntry.annotationType())
                                        .add("$L\n", AnnotationMetaDataSpec.initializer(conditionEntry.annotationMetaData()))
                                        .unindent()
                                        .add("))")
                                        .build()
                        )

        );

        builder.returns(LoadCondition.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return $L.get()", FIELD_NAME)
                .addAnnotation(Override.class)
                .build();
    }

    @Override
    public String methodName() {
        return "condition";
    }
}
