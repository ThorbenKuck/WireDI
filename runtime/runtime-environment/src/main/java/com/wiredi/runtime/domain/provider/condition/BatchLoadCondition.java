package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.conditional.context.ConditionContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BatchLoadCondition extends AbstractLoadCondition {

    private final List<LoadConditionEvaluationStage> evaluators;

    public BatchLoadCondition(List<LoadConditionEvaluationStage> evaluators) {
        this.evaluators = evaluators;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean matches(@NotNull WireRepository wireRepository) {
        if (evaluators.isEmpty()) {
            return true;
        }
        for (LoadConditionEvaluationStage loadConditionEvaluationStage : evaluators) {
            ConditionContext.Runtime context = new ConditionContext.Runtime(wireRepository.environment(), wireRepository.beanContainer(), loadConditionEvaluationStage.annotationMetaData());
            evaluate(wireRepository, context, loadConditionEvaluationStage.type());

            if (!context.isMatched()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public @NotNull LoadCondition add(@NotNull Class<? extends ConditionEvaluator> evaluatorType, @NotNull AnnotationMetaData annotationMetaData) {
        evaluators.add(new LoadConditionEvaluationStage(evaluatorType, annotationMetaData));
        return this;
    }

    public static class Builder {
        private final List<LoadConditionEvaluationStage> evaluators = new ArrayList<>();

        public Builder withEvaluationStage(LoadConditionEvaluationStage stage) {
            this.evaluators.add(stage);
            return this;
        }

        public LoadConditionEvaluationStageBuilder withEvaluationStage(Class<? extends ConditionEvaluator> type) {
            return new LoadConditionEvaluationStageBuilder(this, type);
        }

        public BatchLoadCondition build() {
            BatchLoadCondition result = new BatchLoadCondition(new ArrayList<>(evaluators));
            evaluators.clear();
            return result;
        }

        public static class LoadConditionEvaluationStageBuilder {

            private final Builder builder;
            private final Class<? extends ConditionEvaluator> type;

            public LoadConditionEvaluationStageBuilder(Builder builder, Class<? extends ConditionEvaluator> type) {
                this.builder = builder;
                this.type = type;
            }

            public Builder forAnnotation(AnnotationMetaData annotationMetaData) {
                return builder.withEvaluationStage(new LoadConditionEvaluationStage(type, annotationMetaData));
            }
        }
    }

    @Override
    public String toString() {
        return "All(" + evaluators + ')';
    }
}
