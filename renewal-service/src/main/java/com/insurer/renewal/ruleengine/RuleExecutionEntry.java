package com.insurer.renewal.ruleengine;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RuleExecutionEntry {
    private final Long ruleId;
    private final String ruleCode;
    private final String ruleName;
    private final boolean passed;
    private final String evaluatedValue;
    /** system_code(codeType=RULE_OUTCOME) value actually applied by this rule. */
    private final String outcomeApplied;
}
