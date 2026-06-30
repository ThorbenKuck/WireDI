package com.wiredi.runtime.domain.provider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TypeIdentifierMatcherTest {

    // =====================================================================
    // 1. ROOT FOCUS ALGORITHM TESTS
    // =====================================================================

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("provideRootFocusScenarios")
    @DisplayName("Root Focus Strategy - Verifying that class hierarchy distance takes absolute precedence")
    void testRootFocusStrategy(
            String scenarioName,
            TypeIdentifier<?> target,
            List<TypeIdentifier<?>> inputCandidates,
            List<TypeIdentifier<?>> expectedOrder
    ) {
        // Arrange
        List<TypeIdentifier<?>> shuffledInput = new ArrayList<>(inputCandidates);
        Collections.shuffle(shuffledInput);

        // Act
        List<TypeIdentifier<? extends Object>> actualResult = TypeIdentifierMatcher.filterAndSort(
                shuffledInput,
                (TypeIdentifier<Object>) target,
                TypeIdentifierMatcher.rootFocusStrategy(target)
        );

        // Assert
        assertEquals(expectedOrder.size(), actualResult.size(),
                "Filtered list size mismatch for scenario: " + scenarioName);

        for (int i = 0; i < expectedOrder.size(); i++) {
            assertEquals(expectedOrder.get(i), actualResult.get(i),
                    String.format("Mismatch at index %d for scenario: %s", i, scenarioName));
        }
    }

    private static Stream<Arguments> provideRootFocusScenarios() {
        // Test Case A: Standard Collection hierarchy with CharSequence
        TypeIdentifier<Collection> targetCol = TypeIdentifier.just(Collection.class).withGeneric(CharSequence.class);
        TypeIdentifier<Collection> colCharSequence = TypeIdentifier.just(Collection.class).withGeneric(CharSequence.class);
        TypeIdentifier<Collection> colString = TypeIdentifier.just(Collection.class).withGeneric(String.class);
        TypeIdentifier<List> listCharSequence = TypeIdentifier.just(List.class).withGeneric(CharSequence.class);
        TypeIdentifier<List> listString = TypeIdentifier.just(List.class).withGeneric(String.class);

        // Test Case B: Deep multi-level inheritance (ArrayList -> List -> Collection)
        TypeIdentifier<Collection> targetStringCol = TypeIdentifier.just(Collection.class).withGeneric(String.class);
        TypeIdentifier<ArrayList> arrayListString = TypeIdentifier.just(ArrayList.class).withGeneric(String.class);

        // Test Case C: Multi-Generic Structures (Map<K,V>)
        TypeIdentifier<Map> targetMap = TypeIdentifier.just(Map.class).withGeneric(String.class).withGeneric(CharSequence.class);
        TypeIdentifier<Map> mapStringCharSequence = TypeIdentifier.just(Map.class).withGeneric(String.class).withGeneric(CharSequence.class);
        TypeIdentifier<Map> mapStringString = TypeIdentifier.just(Map.class).withGeneric(String.class).withGeneric(String.class);
        TypeIdentifier<HashMap> hashMapStringCharSequence = TypeIdentifier.just(HashMap.class).withGeneric(String.class).withGeneric(CharSequence.class);
        TypeIdentifier<HashMap> hashMapStringString = TypeIdentifier.just(HashMap.class).withGeneric(String.class).withGeneric(String.class);

        return Stream.of(
                Arguments.of(
                        "Basic Collection Hierarchy - Root Match wins over Generic Match",
                        targetCol,
                        List.of(listString, colString, listCharSequence, colCharSequence),
                        // Expected: Root Distance 0 wins over Root Distance 1.
                        // colCharSequence (0,0) -> colString (0,1) -> listCharSequence (1,0) -> listString (1,1)
                        List.of(colCharSequence, colString, listCharSequence, listString)
                ),
                Arguments.of(
                        "Deep Class Distance - Shortest inheritance path determines order",
                        targetStringCol,
                        List.of(arrayListString, listString, targetStringCol),
                        // Expected Path distances: Collection (0) -> List (1) -> ArrayList (2 via BFS)
                        List.of(targetStringCol, listString, arrayListString)
                ),
                Arguments.of(
                        "Multi-Generic Map Layout - Evaluation of multiple type arguments",
                        targetMap,
                        List.of(hashMapStringString, mapStringString, hashMapStringCharSequence, mapStringCharSequence),
                        // Expected: Maps (Root dist 0) before HashMaps (Root dist 1).
                        // mapStringCharSequence(0,0) -> mapStringString(0,1) -> hashMapStringCharSequence(1,0) -> hashMapStringString(1,1)
                        List.of(mapStringCharSequence, mapStringString, hashMapStringCharSequence, hashMapStringString)
                )
        );
    }

    // =====================================================================
    // 2. GENERIC FOCUS ALGORITHM TESTS
    // =====================================================================

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("provideGenericFocusScenarios")
    @DisplayName("Generic Focus Strategy - Verifying that exact generic matches can override distant root types")
    void testGenericFocusStrategy(
            String scenarioName,
            TypeIdentifier<?> target,
            List<TypeIdentifier<?>> inputCandidates,
            List<TypeIdentifier<?>> expectedOrder
    ) {
        // Arrange
        List<TypeIdentifier<?>> shuffledInput = new ArrayList<>(inputCandidates);
        Collections.shuffle(shuffledInput);

        // Act
        List<TypeIdentifier<?>> actualResult = TypeIdentifierMatcher.filterAndSort(
                shuffledInput,
                (TypeIdentifier<Object>) target,
                TypeIdentifierMatcher.genericFocusStrategy(target)
        );

        // Assert
        assertEquals(expectedOrder.size(), actualResult.size(),
                "Filtered list size mismatch for scenario: " + scenarioName);

        for (int i = 0; i < expectedOrder.size(); i++) {
            assertEquals(expectedOrder.get(i), actualResult.get(i),
                    String.format("Mismatch at index %d for scenario: %s", i, scenarioName));
        }
    }

    private static Stream<Arguments> provideGenericFocusScenarios() {
        // Test Case A: The "B before C" Scenario
        TypeIdentifier<Collection> targetCol = TypeIdentifier.just(Collection.class).withGeneric(CharSequence.class);
        TypeIdentifier<Collection> colCharSequence = TypeIdentifier.just(Collection.class).withGeneric(CharSequence.class);
        TypeIdentifier<Collection> colString = TypeIdentifier.just(Collection.class).withGeneric(String.class);
        TypeIdentifier<List> listCharSequence = TypeIdentifier.just(List.class).withGeneric(CharSequence.class);
        TypeIdentifier<List> listString = TypeIdentifier.just(List.class).withGeneric(String.class);

        // Test Case B: Multi-Generic Maps with Generic priority
        TypeIdentifier<Map> targetMap = TypeIdentifier.just(Map.class).withGeneric(String.class).withGeneric(CharSequence.class);
        TypeIdentifier<Map> mapStringCharSequence = TypeIdentifier.just(Map.class).withGeneric(String.class).withGeneric(CharSequence.class);
        TypeIdentifier<Map> mapStringString = TypeIdentifier.just(Map.class).withGeneric(String.class).withGeneric(String.class);
        TypeIdentifier<HashMap> hashMapStringCharSequence = TypeIdentifier.just(HashMap.class).withGeneric(String.class).withGeneric(CharSequence.class);
        TypeIdentifier<HashMap> hashMapStringString = TypeIdentifier.just(HashMap.class).withGeneric(String.class).withGeneric(String.class);

        return Stream.of(
                Arguments.of(
                        "Collection Hierarchy - Exact Generic Match stabs through distant Roots (B before C)",
                        targetCol,
                        List.of(listString, colString, listCharSequence, colCharSequence),
                        // Expected: Generic Distance 0 wins over Generic Distance 1.
                        // colCharSequence (0,0) -> listCharSequence (0,1) -> colString (1,0) -> listString (1,1)
                        List.of(colCharSequence, listCharSequence, colString, listString)
                ),
                Arguments.of(
                        "Multi-Generic Maps - Perfect Generic alignments bubble up",
                        targetMap,
                        List.of(hashMapStringString, mapStringString, hashMapStringCharSequence, mapStringCharSequence),
                        // Expected: Generic Match 0 wins over Generic Match 1.
                        // mapStringCharSequence(0,0) -> hashMapStringCharSequence(0,1) -> mapStringString(1,0) -> hashMapStringString(1,1)
                        List.of(mapStringCharSequence, hashMapStringCharSequence, mapStringString, hashMapStringString)
                )
        );
    }

    // =====================================================================
    // 3. IS_INSTANCE_OF FILTERING TESTS
    // =====================================================================

    @Test
    @DisplayName("Filtering Enforcement - Ensure incompatible base types and conflicting generics are hard-dropped")
    void testFilteringEnforcement() {
        // Arrange
        TypeIdentifier<List> target = TypeIdentifier.just(List.class).withGeneric(CharSequence.class);

        TypeIdentifier<List> validExact = TypeIdentifier.just(List.class).withGeneric(CharSequence.class);
        TypeIdentifier<ArrayList> validSubRoot = TypeIdentifier.just(ArrayList.class).withGeneric(String.class);

        // Invalid Candidates
        TypeIdentifier<Collection> invalidSuperRoot = TypeIdentifier.just(Collection.class).withGeneric(CharSequence.class); // Collection is not a List
        TypeIdentifier<List> invalidGenericConflict = TypeIdentifier.just(List.class).withGeneric(Integer.class);            // Integer is not a CharSequence
        TypeIdentifier<Map> invalidCompletelyUnrelated = TypeIdentifier.just(Map.class).withGeneric(String.class).withGeneric(String.class);

        List<TypeIdentifier<?>> mixedCandidates = List.of(
                validExact,
                invalidSuperRoot,
                validSubRoot,
                invalidGenericConflict,
                invalidCompletelyUnrelated
        );

        // Act
        // Strategy doesn't matter for looking strictly at the filter retention
        List<TypeIdentifier<? extends List>> filteredResult = TypeIdentifierMatcher.filterAndSort(
                mixedCandidates,
                target,
                TypeIdentifierMatcher.rootFocusStrategy(target)
        );

        // Assert
        assertEquals(2, filteredResult.size(), "Filter failed to drop exactly 3 invalid configurations");
        assertTrue(filteredResult.contains(validExact), "Valid exact match was accidentally removed");
        assertTrue(filteredResult.contains(validSubRoot), "Valid covariant sub-root match was accidentally removed");

        assertFalse(filteredResult.contains(invalidSuperRoot), "Security Breach: Super-interfaces must not pass the filter");
        assertFalse(filteredResult.contains(invalidGenericConflict), "Security Breach: Disjoint type arguments passed the filter");
        assertFalse(filteredResult.contains(invalidCompletelyUnrelated), "Security Breach: Unrelated components passed the filter");
    }
}