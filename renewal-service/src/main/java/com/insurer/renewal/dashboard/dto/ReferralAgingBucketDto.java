package com.insurer.renewal.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class ReferralAgingBucketDto {
    private String bucketLabel; // "0-2 days", "3-5 days", "6+ days"
    private long count;
}
