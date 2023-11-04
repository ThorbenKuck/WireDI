package com.wiredi.processor.plugins;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.AnnotationMetaDataSpec;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;
import com.wiredi.domain.provider.condition.BatchLoadCondition;
import com.wiredi.domain.provider.condition.LoadCondition;
import com.wiredi.domain.provider.condition.LoadConditionEvaluationStage;
import com.wiredi.lang.values.Value;

import javax.lang.model.element.Modifier;
import java.util.List;

public class BatchConditionMethodFactory implements StandaloneMethodFactory {

    private static final String FIELD_NAME = "LOAD_CONDITION";
    private final List<ConditionEntry> conditionEntries;

    public BatchConditionMethodFactory(List<ConditionEntry> conditionEntries) {
        this.conditionEntries = conditionEntries;
    }

    @Override
    public void append(MethodSpec.Builder builder, ClassEntity<?> entity) {
        CodeBlock.Builder initializer = CodeBlock.builder()
                .add("$T.async(() -> $T.newInstance()", Value.class, BatchLoadCondition.class)
                .indent();

        conditionEntries.forEach(entry -> {
            initializer.add("\n.withEvaluationStage(new $T($T.class, $L))", LoadConditionEvaluationStage.class, entry.annotationType(), AnnotationMetaDataSpec.initializer(entry.annotationMetaData()));
        });
        initializer.add("\n.build()").unindent().add("\n)");

        entity.addField(
                ParameterizedTypeName.get(Value.class, LoadCondition.class),
                FIELD_NAME,
                field -> field.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer(initializer.build())
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
