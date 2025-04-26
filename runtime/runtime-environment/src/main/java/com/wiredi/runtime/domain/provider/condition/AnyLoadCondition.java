package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.AnnotationMetaData;
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
    public boolean matches(@NotNull WireRepository wireRepository) {
        for (LoadCondition child : children) {
            if (child.matches(wireRepository)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @NotNull LoadCondition or(@NotNull LoadCondition other) {
        children.add(other);
        return this;
    }

    @Override
    public @NotNull LoadCondition add(@NotNull Class<? extends ConditionEvaluator> evaluatorType, @NotNull AnnotationMetaData annotationMetaData) {
        return or(new SingleLoadCondition(evaluatorType, annotationMetaData));
    }

    @Override
    public String toString() {
        return "Or{" + children + '}';
    }
}
