package com.insurer.renewal.ruleengine;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConditionEvaluatorTest {

    @ParameterizedTest(name = "{0} {1} {2} => {3}")
    @CsvSource({
            "5, GREATER_THAN, 3, true",
            "3, GREATER_THAN, 5, false",
            "5, LESS_THAN, 10, true",
            "5, GREATER_THAN_OR_EQUAL, 5, true",
            "5, LESS_THAN_OR_EQUAL, 5, true",
            "4, LESS_THAN_OR_EQUAL, 5, true",
    })
    void numericComparisons(String actual, String operator, String expected, boolean expectedResult) {
        assertEquals(expectedResult, ConditionEvaluator.evaluate(Integer.valueOf(actual), operator, expected));
    }

    @Test
    void equalsIsCaseInsensitiveForStrings() {
        assertTrue(ConditionEvaluator.evaluate("ACTIVE", "EQUALS", "active"));
        assertFalse(ConditionEvaluator.evaluate("ACTIVE", "EQUALS", "cancelled"));
    }

    @Test
    void notEqualsNegatesEquals() {
        assertTrue(ConditionEvaluator.evaluate("ACTIVE", "NOT_EQUALS", "CANCELLED"));
        assertFalse(ConditionEvaluator.evaluate("ACTIVE", "NOT_EQUALS", "ACTIVE"));
    }

    @Test
    void inListMatchesAnyMemberCaseInsensitively() {
        assertTrue(ConditionEvaluator.evaluate("active", "IN", "[\"ACTIVE\",\"PENDING\"]"));
        assertFalse(ConditionEvaluator.evaluate("cancelled", "IN", "[\"ACTIVE\",\"PENDING\"]"));
    }

    @Test
    void notInIsNegationOfIn() {
        assertFalse(ConditionEvaluator.evaluate("active", "NOT_IN", "[\"ACTIVE\",\"PENDING\"]"));
        assertTrue(ConditionEvaluator.evaluate("cancelled", "NOT_IN", "[\"ACTIVE\",\"PENDING\"]"));
    }

    @Test
    void betweenIsInclusiveOnBothBounds() {
        assertTrue(ConditionEvaluator.evaluate(50, "BETWEEN", "[50, 75]"));
        assertTrue(ConditionEvaluator.evaluate(75, "BETWEEN", "[50, 75]"));
        assertFalse(ConditionEvaluator.evaluate(49, "BETWEEN", "[50, 75]"));
        assertFalse(ConditionEvaluator.evaluate(76, "BETWEEN", "[50, 75]"));
    }

    @Test
    void booleanEquals() {
        assertTrue(ConditionEvaluator.evaluate(Boolean.TRUE, "EQUALS", "true"));
        assertFalse(ConditionEvaluator.evaluate(Boolean.FALSE, "EQUALS", "true"));
    }

    @Test
    void missingFieldNeverMatches() {
        assertFalse(ConditionEvaluator.evaluate(null, "EQUALS", "ACTIVE"));
        assertFalse(ConditionEvaluator.evaluate(null, "NOT_EQUALS", "ACTIVE"));
    }

    @Test
    void unknownOperatorCodeThrows() {
        // Guards against a system_code row existing for a "catalog" operator
        // that ConditionEvaluator hasn't actually implemented yet.
        assertThrows(IllegalArgumentException.class,
                () -> ConditionEvaluator.evaluate("x", "STARTS_WITH", "x"));
    }
}
