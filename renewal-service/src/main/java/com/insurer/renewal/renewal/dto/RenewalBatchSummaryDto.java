package com.insurer.renewal.renewal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @Builder @AllArgsConstructor
public class RenewalBatchSummaryDto {
    private Long id;
    private LocalDateTime runDate;

    /** system_code(codeType=BATCH_STATUS). */
    private String status;

    private int totalPoliciesEvaluated;
    private int eligibleCount;
    private int referCount;
    private int ineligibleCount;
}
