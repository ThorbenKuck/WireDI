package com.wiredi.domain.provider.condition;

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

    public static class Builder {
        private final List<LoadConditionEvaluationStage> evaluators = new ArrayList<>();

        public Builder withEvaluationStage(LoadConditionEvaluationStage stage) {
            this.evaluators.add(stage);
            return this;
        }

        public BatchLoadCondition build() {
            BatchLoadCondition result = new BatchLoadCondition(new ArrayList<>(evaluators));
            evaluators.clear();
            return result;
        }
    }
}
