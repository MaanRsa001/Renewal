package com.insurer.renewal.dashboard;

import com.insurer.renewal.dashboard.dto.*;
import com.insurer.renewal.referral.ReferralCase;
import com.insurer.renewal.referral.repository.ReferralCaseRepository;
import com.insurer.renewal.renewal.repository.RenewalCaseRepository;
import com.insurer.renewal.renewal.repository.RenewalRuleExecutionLogRepository;
import com.insurer.renewal.security.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final RenewalCaseRepository renewalCaseRepository;
    private final RenewalRuleExecutionLogRepository executionLogRepository;
    private final ReferralCaseRepository referralCaseRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public DashboardSummaryDto getSummary(LocalDateTime from, LocalDateTime to, String productCode, String countryCode) {
        List<RenewalCaseRepository.DecisionCountProjection> rows =
                renewalCaseRepository.countByDecisionInRange(from, to, productCode, countryCode);

        Map<String, Long> counts = rows.stream()
                .collect(Collectors.toMap(RenewalCaseRepository.DecisionCountProjection::getDecision,
                        RenewalCaseRepository.DecisionCountProjection::getCnt));

        long eligible = counts.getOrDefault("ELIGIBLE", 0L);
        long refer = counts.getOrDefault("REFER", 0L);
        long ineligible = counts.getOrDefault("INELIGIBLE", 0L);
        long total = eligible + refer + ineligible;

        return DashboardSummaryDto.builder()
                .totalEvaluated(total)
                .eligibleCount(eligible)
                .referCount(refer)
                .ineligibleCount(ineligible)
                .eligiblePct(pct(eligible, total))
                .referPct(pct(refer, total))
                .ineligiblePct(pct(ineligible, total))
                .build();
    }

    @Override
    public List<ReferralAgingBucketDto> getReferralAging() {
        List<ReferralCase> open = referralCaseRepository.findAllOpenOrInReview();
        LocalDateTime now = LocalDateTime.now();

        long zeroToTwo = open.stream().filter(rc -> ageDays(rc, now) <= 2).count();
        long threeToFive = open.stream().filter(rc -> { long d = ageDays(rc, now); return d >= 3 && d <= 5; }).count();
        long sixPlus = open.stream().filter(rc -> ageDays(rc, now) >= 6).count();

        return List.of(
                ReferralAgingBucketDto.builder().bucketLabel("0-2 days").count(zeroToTwo).build(),
                ReferralAgingBucketDto.builder().bucketLabel("3-5 days").count(threeToFive).build(),
                ReferralAgingBucketDto.builder().bucketLabel("6+ days").count(sixPlus).build()
        );
    }

    @Override
    public List<RuleHitFrequencyDto> getRuleHitFrequency() {
        return executionLogRepository.findRuleHitFrequency().stream()
                .map(p -> RuleHitFrequencyDto.builder()
                        .ruleCode(p.getRuleCode())
                        .outcome(p.getOutcome())
                        .hits(p.getHits())
                        .build())
                .toList();
    }

    @Override
    public List<UnderwriterWorkloadDto> getUnderwriterWorkload() {
        return referralCaseRepository.findWorkloadByUnderwriter().stream()
                .map(p -> UnderwriterWorkloadDto.builder()
                        .userId(p.getUserId())
                        .username(appUserRepository.findById(p.getUserId())
                                .map(u -> u.getFullName()).orElse("Unknown"))
                        .openReferralCount(p.getOpenCount())
                        .build())
                .toList();
    }

    private long ageDays(ReferralCase rc, LocalDateTime now) {
        return ChronoUnit.DAYS.between(rc.getCreatedAt(), now);
    }

    private double pct(long part, long total) {
        return total == 0 ? 0.0 : Math.round((part * 10000.0) / total) / 100.0;
    }
}
