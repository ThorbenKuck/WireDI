package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;

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
    public boolean matches(WireRepository wireRepository) {
        for (LoadCondition child : children) {
            if (!child.matches(wireRepository)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public LoadCondition add(Class<? extends ConditionEvaluator> evaluatorType, AnnotationMetaData annotationMetaData) {
        return and(new SingleLoadCondition(evaluatorType, annotationMetaData));
    }

    @Override
    public LoadCondition and(LoadCondition other) {
        children.add(other);
        return this;
    }

    @Override
    public String toString() {
        return "And{" + children + '}';
    }
}
