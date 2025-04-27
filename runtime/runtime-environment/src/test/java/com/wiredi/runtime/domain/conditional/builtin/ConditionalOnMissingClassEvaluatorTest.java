package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.context.ConditionContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class ConditionalOnMissingClassEvaluatorTest {

    private final ConditionContext.Runtime context = Mockito.mock(ConditionContext.Runtime.class);
    private final ConditionalOnMissingClassEvaluator evaluator = new ConditionalOnMissingClassEvaluator();

    @Test
    void shouldPassWhenClassDoesNotExist() {
        // Arrange
        AnnotationMetaData annotationMetaData = AnnotationMetaData.builder(ConditionalOnMissingClass.class)
                .withField("className", "com.nonexistent.Class")
                .build();
        when(context.annotationMetaData()).thenReturn(annotationMetaData);

        // Act & Assert
        assertDoesNotThrow(() -> evaluator.testRuntimeCondition(context));
        verify(context, never()).failAndStop(anyString());
    }

    @Test
    void shouldFailWhenClassExists() {
        // Arrange
        AnnotationMetaData annotationMetaData = AnnotationMetaData.builder(ConditionalOnMissingClass.class)
                .withField("className", "java.lang.String")
                .build();
        when(context.annotationMetaData()).thenReturn(annotationMetaData);

        // Act
        evaluator.testRuntimeCondition(context);

        // Assert
        verify(context).failAndStop(contains("Class was found but should be missing"));
    }
}
