package com.insurer.renewal.dashboard;

import com.insurer.renewal.dashboard.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryDto summary(
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String product,
            @RequestParam(required = false) String country) {
        return dashboardService.getSummary(from, to, product, country);
    }

    @GetMapping("/referral-aging")
    public List<ReferralAgingBucketDto> referralAging() {
        return dashboardService.getReferralAging();
    }

    @GetMapping("/rule-hit-frequency")
    public List<RuleHitFrequencyDto> ruleHitFrequency() {
        return dashboardService.getRuleHitFrequency();
    }

    @GetMapping("/underwriter-workload")
    public List<UnderwriterWorkloadDto> underwriterWorkload() {
        return dashboardService.getUnderwriterWorkload();
    }
}
