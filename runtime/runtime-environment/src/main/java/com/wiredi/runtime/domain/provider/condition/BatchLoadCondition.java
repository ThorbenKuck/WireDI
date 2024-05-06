package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.WireRepository;

import java.util.ArrayList;
import java.util.List;

public class BatchLoadCondition extends AbstractLoadCondition {

    private final List<LoadConditionEvaluationStage> evaluators;

    public BatchLoadCondition(List<LoadConditionEvaluationStage> evaluators) {
        this.evaluators = evaluators;
    }

    public static Builder newInstance() {
        return new Builder();
    }

    @Override
    public boolean matches(WireRepository wireRepository) {
        if (evaluators.isEmpty()) {
            return true;
        }
        for (LoadConditionEvaluationStage loadConditionEvaluationStage : evaluators) {
            if (!evaluate(wireRepository, loadConditionEvaluationStage.type(), loadConditionEvaluationStage.annotationMetaData())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public LoadCondition add(Class<? extends ConditionEvaluator> evaluatorType, AnnotationMetaData annotationMetaData) {
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
