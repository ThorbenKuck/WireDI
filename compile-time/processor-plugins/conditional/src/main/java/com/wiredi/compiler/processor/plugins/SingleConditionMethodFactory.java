package com.wiredi.compiler.processor.plugins;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.wiredi.annotations.Wire;
import com.wiredi.compiler.domain.AnnotationMetaDataSpec;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;
import com.wiredi.runtime.domain.provider.condition.EagerLoadCondition;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import com.wiredi.runtime.domain.provider.condition.SingleLoadCondition;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.util.List;

public class SingleConditionMethodFactory implements StandaloneMethodFactory {

    private static final String FIELD_NAME = "LOAD_CONDITION";
    private final ConditionEntry conditionEntry;

    public SingleConditionMethodFactory(ConditionEntry conditionEntry) {
        this.conditionEntry = conditionEntry;
    }

    @Override
    public void append(MethodSpec.@NotNull Builder builder, @NotNull ClassEntity<?> entity) {
//        Class<? extends ConditionEvaluator> evaluatorType = conditionEntry.annotationMetaData().requireClass("value");
        boolean isWired = Annotations.isAnnotatedWith(conditionEntry.evaluatorTypeElement(), Wire.class);
        List<? extends Element> constructors = conditionEntry.evaluatorTypeElement()
                .getEnclosedElements()
                .stream()
                .filter(it -> it.getKind() == ElementKind.CONSTRUCTOR)
                .toList();

        if (!isWired && constructors.size() == 0 || (constructors.size() > 1 && constructors.getFirst().getEnclosedElements().stream().noneMatch(it -> it.getKind() == ElementKind.PARAMETER))) {
            appendDirectInstantiation(entity);
        } else {
            appendRuntimeInstantiation(entity);
        }

        builder.returns(LoadCondition.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return $L.get()", FIELD_NAME)
                .addAnnotation(Override.class)
                .build();
    }

    private void appendRuntimeInstantiation(ClassEntity<?> entity) {
        entity.addField(
                ParameterizedTypeName.get(Value.class, LoadCondition.class),
                FIELD_NAME,
                field -> field.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer(
                                CodeBlock.builder()
                                        .add("$T.async(() -> new $T(\n", Value.class, SingleLoadCondition.class)
                                        .indent()
                                        .add("$T.class,\n", conditionEntry.evaluatorType())
                                        .add("$L\n", AnnotationMetaDataSpec.initializer(conditionEntry.annotationMetaData()))
                                        .unindent()
                                        .add("))")
                                        .build()
                        )

        );
    }

    private void appendDirectInstantiation(ClassEntity<?> entity) {
        entity.addField(
                ParameterizedTypeName.get(Value.class, LoadCondition.class),
                FIELD_NAME,
                field -> field.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer(
                                CodeBlock.builder()
                                        .add("$T.async(() -> new $T(\n", Value.class, EagerLoadCondition.class)
                                        .indent()
                                        .add("new $T(),\n", conditionEntry.evaluatorType())
                                        .add("$L\n", AnnotationMetaDataSpec.initializer(conditionEntry.annotationMetaData()))
                                        .unindent()
                                        .add("))")
                                        .build()
                        )

        );
    }

    @Override
    public @NotNull String methodName() {
        return "condition";
    }
}
