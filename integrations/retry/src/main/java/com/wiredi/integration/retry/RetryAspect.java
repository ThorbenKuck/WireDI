package com.wiredi.integration.retry;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Wire;
import com.wiredi.annotations.aspects.Aspect;
import com.wiredi.runtime.aspects.ExecutionContext;
import com.wiredi.runtime.aspects.RootMethod;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.retry.RetryTemplate;
import com.wiredi.runtime.retry.backoff.BackOffStrategy;
import com.wiredi.runtime.retry.policy.RetryPolicy;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Wire
@Order(RetryAspect.ORDER)
public class RetryAspect {

    public static final int ORDER = Order.FIRST + 100;
    private static final Map<RootMethod, RetryTemplate> retryTemplates = new HashMap<>();

    private static RetryTemplate getRetryTemplate(ExecutionContext context) {
        return retryTemplates.computeIfAbsent(context.getRootMethod(), (m) -> {
            AnnotationMetaData retryAnnotation = context.findAnnotation(Retry.class).orElseThrow();
            AnnotationMetaData backoffAnnotation = retryAnnotation.getAnnotation("backoff", Backoff.DEFAULT_META_DATA);

            RetryPolicy.Builder retryPolicyBuilder = RetryPolicy.builder()
                    .withDelay(Duration.of(
                            backoffAnnotation.getLong("value", 0),
                            backoffAnnotation.getEnum("backoffUnit", TimeUnit.MILLISECONDS).toChronoUnit()
                    ));

            setMaxRetries(retryAnnotation, retryPolicyBuilder);
            setMaxTimeout(retryAnnotation, retryPolicyBuilder);

            return RetryTemplate.builder()
                    .withRetryPolicy(retryPolicyBuilder.build())
                    .withBackOff(backOffStrategy(backoffAnnotation))
                    .build();
        });
    }

    private static void setMaxTimeout(AnnotationMetaData retryAnnotation, RetryPolicy.Builder retryPolicyBuilder) {
        long maxTimeout = retryAnnotation.getLong("maxTimeout", -1);
        if (maxTimeout < 0) {
            retryPolicyBuilder.withoutMaxTimeout();
        } else {
            retryPolicyBuilder.withMaxTimeout(Duration.of(
                    maxTimeout,
                    retryAnnotation.getEnum("maxTimeout", TimeUnit.MILLISECONDS).toChronoUnit()
            ));
        }
    }

    private static void setMaxRetries(AnnotationMetaData retryAnnotation, RetryPolicy.Builder retryPolicyBuilder) {
        long maxRetries = retryAnnotation.getLong("maxRetries", -1);
        if (maxRetries < 0) {
            retryPolicyBuilder.withIndefiniteAttempts();
        } else {
            retryPolicyBuilder.withMaxRetries(maxRetries);
        }
    }

    private static BackOffStrategy<?> backOffStrategy(AnnotationMetaData backoffAnnotation) {
        long value = backoffAnnotation.getLong("value", 0);
        if (value < 0) {
            return BackOffStrategy.none();
        }

        double multiplier = backoffAnnotation.getDouble("multiplier", 1.0);
        if (multiplier == 1.0) {
            return BackOffStrategy.fixed(Duration.of(value, backoffAnnotation.getEnum("backoffUnit", TimeUnit.MILLISECONDS).toChronoUnit()));
        } else {
            return BackOffStrategy.exponential(multiplier);
        }
    }

    @Aspect(around = Retry.class)
    public Object handleRetryInvocation(ExecutionContext context) {
        RetryTemplate template = getRetryTemplate(context);
        return template.tryGet(context::proceed);
    }
}
