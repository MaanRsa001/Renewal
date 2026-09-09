package com.insurer.renewal.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class UnderwriterWorkloadDto {
    private Long userId;
    private String username;
    private long openReferralCount;
}
