package com.wiredi.runtime.domain.conditional.context;

import com.wiredi.runtime.Environment;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.beans.BeanContainer;
import com.wiredi.runtime.domain.AnnotationMetaData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public sealed interface ConditionContext {

    Environment environment();

    AnnotationMetaData annotationMetaData();

    ConditionContext noteDependency(String dependency);

    boolean isMatched();

    void failAndStop();

    void failAndStop(String details);

    static ConditionContext runtime(WireRepository wireRepository, AnnotationMetaData annotationMetaData) {
        return new ConditionContext.Runtime(wireRepository.environment(), wireRepository.beanContainer(), annotationMetaData);
    }

    static ConditionContext statically(WireRepository wireRepository, AnnotationMetaData annotationMetaData) {
        return new ConditionContext.Runtime(wireRepository.environment(), wireRepository.beanContainer(), annotationMetaData);
    }

    default ConditionContext noteDependencies(String... dependencies) {
        for (String dependency : dependencies) {
            noteDependency(dependency);
        }

        return this;
    }

    default ConditionContext noteDependencies(Collection<String> dependencies) {
        for (String dependency : dependencies) {
            noteDependency(dependency);
        }

        return this;
    }

    final class Runtime extends BasicConditionContext implements ConditionContext {

        @NotNull final BeanContainer beanContainer;

        public Runtime(@NotNull Environment environment, @NotNull BeanContainer beanContainer, @NotNull AnnotationMetaData annotationMetaData) {
            super(environment, annotationMetaData);
            this.beanContainer = beanContainer;
        }

        public BeanContainer beanContainer() {
            return beanContainer;
        }

        @Override
        public ConditionContext noteDependency(String dependency) {
            this.noteSingleDependency(dependency);
            return this;
        }
    }

    final class Static extends BasicConditionContext implements ConditionContext {

        public Static(@NotNull Environment environment, @NotNull AnnotationMetaData annotationMetaData) {
            super(environment, annotationMetaData);
        }

        @Override
        public ConditionContext noteDependency(String dependency) {
            this.noteSingleDependency(dependency);
            return this;
        }
    }

    class BasicConditionContext {

        @NotNull final Environment environment;
        @NotNull final AnnotationMetaData annotationMetaData;
        @NotNull final List<String> dependencies = new ArrayList<>();
        @NotNull final List<String> details = new ArrayList<>();
        boolean matched = true;

        public BasicConditionContext(@NotNull Environment environment, @NotNull AnnotationMetaData annotationMetaData) {
            this.environment = environment;
            this.annotationMetaData = annotationMetaData;
        }

        public Environment environment() {
            return environment;
        }

        public AnnotationMetaData annotationMetaData() {
            return annotationMetaData;
        }

        public void noteSingleDependency(String dependency) {
            this.dependencies.add(dependency);
        }

        public void failAndStop() {
            this.matched = false;
        }

        public void failAndStop(String details) {
            this.matched = false;
            this.details.add(details);
        }

        public boolean isMatched() {
            return matched;
        }
    }
}
