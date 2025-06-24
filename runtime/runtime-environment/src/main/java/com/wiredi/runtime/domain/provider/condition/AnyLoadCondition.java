package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnyLoadCondition implements LoadCondition {

    private final List<LoadCondition> children;

    public AnyLoadCondition(LoadCondition... children) {
        this(Arrays.asList(children));
    }

    public AnyLoadCondition(List<LoadCondition> children) {
        this.children = new ArrayList<>(children);
    }

    @Override
    public void test(ConditionEvaluation.Context context) {
        for (LoadCondition child : children) {
            child.test(context);
            if (!context.isMatched()) {
                return;
            }
        }
    }

    @Override
    public @NotNull LoadCondition or(@NotNull LoadCondition other) {
        children.add(other);
        return this;
    }

    @Override
    public @NotNull LoadCondition add(@NotNull Class<? extends ConditionEvaluator> evaluatorType, @NotNull AnnotationMetadata annotationMetaData) {
        return or(new SingleLoadCondition(evaluatorType, annotationMetaData));
    }

    @Override
    public String toString() {
        return "Or{" + children + '}';
    }
}
