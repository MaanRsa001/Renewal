package com.insurer.renewal.dashboard;

import com.insurer.renewal.dashboard.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface DashboardService {

    DashboardSummaryDto getSummary(LocalDateTime from, LocalDateTime to, String productCode, String countryCode);

    List<ReferralAgingBucketDto> getReferralAging();

    List<RuleHitFrequencyDto> getRuleHitFrequency();

    List<UnderwriterWorkloadDto> getUnderwriterWorkload();
}
