package com.insurer.renewal.renewal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Builder @AllArgsConstructor
public class RenewalCaseDto {
    private Long id;
    private String policyNo;

    /** system_code(codeType=RULE_OUTCOME). */
    private String decision;

    private String decisionReason;
    private boolean overridden;
    private String overrideReason;
    private LocalDateTime evaluatedAt;
    private List<RuleExecutionLogDto> executionLog;
}
