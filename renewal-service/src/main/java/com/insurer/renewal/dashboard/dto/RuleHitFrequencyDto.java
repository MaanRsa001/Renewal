package com.insurer.renewal.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class RuleHitFrequencyDto {
    private String ruleCode;

    /** system_code(codeType=RULE_OUTCOME). */
    private String outcome;

    private long hits;
}
