package com.wiredi.runtime.domain.provider;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnClass;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnClassEvaluator;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingClassEvaluator;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class LoadConditionBuilderTest {

    @Test
    void shouldCreateSimpleCondition() {
        // Arrange & Act
        LoadCondition condition = LoadCondition.forEvaluator(ConditionalOnClassEvaluator.class)
                .withAnnotation(
                        AnnotationMetaData.newInstance(ConditionalOnClass.class)
                                .withField("className", "java.lang.String")
                                .build()
                )
                .build();

        // Assert
        assertNotNull(condition);
        WireRepository repository = mock(WireRepository.class);
        assertTrue(condition.matches(repository));
    }

    @Test
    void shouldCreateConditionWithCustomAnnotationName() {
        // Arrange & Act
        LoadCondition condition = LoadCondition.forEvaluator(ConditionalOnClassEvaluator.class)
                .withAnnotation(
                        AnnotationMetaData.newInstance("CustomAnnotation")
                                .withField("className", "java.lang.String")
                                .build()
                )
                .build();

        // Assert
        assertNotNull(condition);
        WireRepository repository = mock(WireRepository.class);
        assertTrue(condition.matches(repository));
    }

    @Test
    void shouldCreateComplexConditionDirectly() {
        // Arrange & Act
        LoadCondition condition = LoadCondition.forEvaluator(ConditionalOnClassEvaluator.class)
                .withAnnotation(
                        AnnotationMetaData.newInstance("CustomAnnotation")
                                .withField("className", "java.lang.String")
                                .build()
                )
                .build()
                .and(
                        LoadCondition.forEvaluator(ConditionalOnMissingClassEvaluator.class)
                                .withAnnotation(
                                        AnnotationMetaData.newInstance("CustomAnnotation")
                                                .withField("className", "com.nonexisting.class")
                                                .build())
                                .build()
                );

        // Assert
        WireRepository repository = mock(WireRepository.class);
        assertTrue(condition.matches(repository));
    }
}
