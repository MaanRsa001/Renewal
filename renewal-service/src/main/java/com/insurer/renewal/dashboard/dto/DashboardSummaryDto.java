package com.insurer.renewal.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class DashboardSummaryDto {
    private long totalEvaluated;
    private long eligibleCount;
    private long referCount;
    private long ineligibleCount;
    private double eligiblePct;
    private double referPct;
    private double ineligiblePct;
}
