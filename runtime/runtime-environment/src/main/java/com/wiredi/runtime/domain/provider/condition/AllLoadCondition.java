package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AllLoadCondition implements LoadCondition {

    private final List<LoadCondition> children;

    public AllLoadCondition(LoadCondition... children) {
        this(Arrays.asList(children));
    }

    public AllLoadCondition(List<LoadCondition> children) {
        this.children = new ArrayList<>(children);
    }

    @Override
    public void test(ConditionEvaluation.Context context) {
        for (LoadCondition child : children) {
            child.test(context);
        }
    }

    @Override
    public @NotNull LoadCondition add(@NotNull Class<? extends ConditionEvaluator> evaluatorType, @NotNull AnnotationMetadata annotationMetaData) {
        return and(new SingleLoadCondition(evaluatorType, annotationMetaData));
    }

    @Override
    public @NotNull LoadCondition and(@NotNull LoadCondition other) {
        children.add(other);
        return this;
    }

    @Override
    public String toString() {
        return "And{" + children + '}';
    }
}
