package com.insurer.renewal.referral.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @Builder @AllArgsConstructor
public class ReferralHistoryDto {
    private String previousStatus;
    private String newStatus;
    private String explanation;
    private Long performedBy;
    private LocalDateTime performedAt;
}
