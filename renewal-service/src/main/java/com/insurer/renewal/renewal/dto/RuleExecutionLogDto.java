package com.insurer.renewal.renewal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class RuleExecutionLogDto {
    private String ruleCode;
    private String result;          // PASS / FAIL
    private String evaluatedValue;

    /** system_code(codeType=RULE_OUTCOME). */
    private String outcomeApplied;
}
