package com.insurer.renewal.referral.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @Builder @AllArgsConstructor
public class ReferralCaseDto {
    private Long id;
    private String policyNo;
    private String productCode;
    private String decisionReason;      // why the rule engine referred it

    /** system_code(codeType=REFERRAL_STATUS). */
    private String status;

    /** system_code(codeType=REFERRAL_PRIORITY). */
    private String priority;

    private Long assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
}
