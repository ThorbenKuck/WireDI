package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.context.ConditionContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class ConditionalOnClassEvaluatorTest {

    private final ConditionContext.Runtime context = Mockito.mock(ConditionContext.Runtime.class);
    private final ConditionalOnClassEvaluator evaluator = new ConditionalOnClassEvaluator();

    @Test
    void shouldPassWhenClassExists() {
        // Arrange
        AnnotationMetaData annotationMetaData = AnnotationMetaData.newInstance(ConditionalOnClass.class)
                .withField("className", "java.lang.String")
                .build();
        when(context.annotationMetaData()).thenReturn(annotationMetaData);

        // Act & Assert
        assertDoesNotThrow(() -> evaluator.testRuntimeCondition(context));
        verify(context, never()).failAndStop(anyString());
    }

    @Test
    void shouldFailWhenClassDoesNotExist() {
        // Arrange
        AnnotationMetaData annotationMetaData = AnnotationMetaData.newInstance(ConditionalOnClass.class)
                .withField("className", "com.nonexistent.Class")
                .build();
        when(context.annotationMetaData()).thenReturn(annotationMetaData);

        // Act
        evaluator.testRuntimeCondition(context);

        // Assert
        verify(context).failAndStop(contains("Required class not found"));
    }
}
