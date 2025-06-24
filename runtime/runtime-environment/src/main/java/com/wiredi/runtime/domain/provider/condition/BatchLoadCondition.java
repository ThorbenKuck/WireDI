package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BatchLoadCondition implements LoadCondition {

    private final List<LoadConditionEvaluationStage> evaluators;

    public BatchLoadCondition(List<LoadConditionEvaluationStage> evaluators) {
        this.evaluators = evaluators;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void test(ConditionEvaluation.Context context) {
        if (evaluators.isEmpty()) {
            return;
        }

        for (LoadConditionEvaluationStage loadConditionEvaluationStage : evaluators) {
            ConditionEvaluator evaluator = context.get(loadConditionEvaluationStage.type());
            context.withAnnotationMetadata(loadConditionEvaluationStage.annotationMetaData(), evaluator::test);
        }
    }

    @Override
    public @NotNull LoadCondition add(@NotNull Class<? extends ConditionEvaluator> evaluatorType, @NotNull AnnotationMetadata annotationMetaData) {
        evaluators.add(new LoadConditionEvaluationStage(evaluatorType, annotationMetaData));
        return this;
    }

    @Override
    public String toString() {
        return "All(" + evaluators + ')';
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

            public Builder forAnnotation(AnnotationMetadata annotationMetaData) {
                return builder.withEvaluationStage(new LoadConditionEvaluationStage(type, annotationMetaData));
            }
        }
    }
}
