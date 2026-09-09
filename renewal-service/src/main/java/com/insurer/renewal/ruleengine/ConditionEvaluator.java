package com.insurer.renewal.ruleengine;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

/**
 * Evaluates a single condition (field/operator/value) against a value pulled
 * from a PolicySnapshot. The operator is now a plain String code (validated
 * at rule-authoring time against system_code(codeType=CONDITION_OPERATOR))
 * rather than a Java enum - the CATALOG of available operators is
 * data-driven, but the actual comparison logic per operator is still real
 * code here, since "evaluate BETWEEN" is a computation, not just a label.
 * Adding a genuinely new comparison still requires a code change here and
 * a matching system_code row; this class is not itself made obsolete by
 * the DB-driven catalog.
 */
public class ConditionEvaluator {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ConditionEvaluator() {}

    public static boolean evaluate(Object actualValue, String operatorCode, String conditionValue) {
        if (actualValue == null) {
            // Unknown/missing field: treat every comparison as non-matching, including
            // NOT_EQUALS/NOT_IN, to avoid false "passes" on missing data.
            return false;
        }

        return switch (operatorCode.toUpperCase()) {
            case "EQUALS" -> equalsValue(actualValue, conditionValue);
            case "NOT_EQUALS" -> !equalsValue(actualValue, conditionValue);
            case "IN" -> inList(actualValue, conditionValue);
            case "NOT_IN" -> !inList(actualValue, conditionValue);
            case "GREATER_THAN" -> compareNumeric(actualValue, conditionValue) > 0;
            case "LESS_THAN" -> compareNumeric(actualValue, conditionValue) < 0;
            case "GREATER_THAN_OR_EQUAL" -> compareNumeric(actualValue, conditionValue) >= 0;
            case "LESS_THAN_OR_EQUAL" -> compareNumeric(actualValue, conditionValue) <= 0;
            case "BETWEEN" -> between(actualValue, conditionValue);
            default -> throw new IllegalArgumentException("Unsupported condition operator: " + operatorCode);
        };
    }

    private static boolean equalsValue(Object actual, String expected) {
        if (isNumeric(actual) && isNumericString(expected)) {
            return toBigDecimal(actual).compareTo(new BigDecimal(expected)) == 0;
        }
        if (actual instanceof Boolean) {
            return actual.equals(Boolean.parseBoolean(expected));
        }
        return String.valueOf(actual).equalsIgnoreCase(expected);
    }

    private static boolean inList(Object actual, String jsonArray) {
        List<String> values = parseStringArray(jsonArray);
        String actualStr = String.valueOf(actual);
        return values.stream().anyMatch(v -> v.equalsIgnoreCase(actualStr));
    }

    private static boolean between(Object actual, String jsonArray) {
        List<String> bounds = parseStringArray(jsonArray);
        if (bounds.size() != 2) {
            throw new IllegalArgumentException("BETWEEN requires exactly 2 values, got: " + jsonArray);
        }
        BigDecimal value = toBigDecimal(actual);
        BigDecimal min = new BigDecimal(bounds.get(0));
        BigDecimal max = new BigDecimal(bounds.get(1));
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    private static int compareNumeric(Object actual, String expected) {
        return toBigDecimal(actual).compareTo(new BigDecimal(expected));
    }

    private static boolean isNumeric(Object value) {
        return value instanceof Number;
    }

    private static boolean isNumericString(String s) {
        try {
            new BigDecimal(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    private static List<String> parseStringArray(String json) {
        try {
            List<Object> raw = MAPPER.readValue(json, List.class);
            return raw.stream().map(String::valueOf).toList();
        } catch (Exception e) {
            throw new IllegalArgumentException("Expected a JSON array for IN/BETWEEN value, got: " + json, e);
        }
    }
}
