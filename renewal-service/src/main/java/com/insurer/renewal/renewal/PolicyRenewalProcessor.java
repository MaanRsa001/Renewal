package com.insurer.renewal.renewal;

import com.insurer.renewal.common.ResourceNotFoundException;
import com.insurer.renewal.policy.Policy;
import com.insurer.renewal.policy.PolicyClaimSummary;
import com.insurer.renewal.policy.PolicyClaimSummaryRepository;
import com.insurer.renewal.product.RenewalRuleSet;
import com.insurer.renewal.product.repository.RenewalRuleSetRepository;
import com.insurer.renewal.referral.ReferralCase;
import com.insurer.renewal.referral.repository.ReferralCaseRepository;
import com.insurer.renewal.renewal.repository.RenewalCaseRepository;
import com.insurer.renewal.renewal.repository.RenewalOfferRepository;
import com.insurer.renewal.ruleengine.PolicySnapshot;
import com.insurer.renewal.ruleengine.RuleEngineService;
import com.insurer.renewal.ruleengine.RuleEvaluationResult;
import com.insurer.renewal.ruleengine.RuleExecutionEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Evaluates and persists ONE policy's renewal case.
 * Deliberately a separate Spring bean (not a private/protected method inside
 * RenewalBatchServiceImpl) so REQUIRES_NEW actually goes through the
 * transactional proxy - a same-class self-invocation would silently run in
 * the caller's existing transaction instead, defeating the per-policy
 * isolation the batch loop depends on.
 */
@Component
@RequiredArgsConstructor
public class PolicyRenewalProcessor {

    private final PolicyClaimSummaryRepository claimSummaryRepository;
    private final RenewalRuleSetRepository ruleSetRepository;
    private final RuleEngineService ruleEngineService;
    private final RenewalCaseRepository renewalCaseRepository;
    private final RenewalOfferRepository renewalOfferRepository;
    private final ReferralCaseRepository referralCaseRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String process(Policy policy, RenewalBatchRun batchRun) {
        LocalDate today = LocalDate.now();

        RenewalRuleSet ruleSet = ruleSetRepository.findActiveRuleSet(
                        policy.getProduct().getId(),
                        policy.getLineOfBusiness() != null ? policy.getLineOfBusiness().getId() : null,
                        policy.getCountry().getId(),
                        today)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active renewal rule set for product=" + policy.getProduct().getCode()
                                + " country=" + policy.getCountry().getCode()));

        PolicyClaimSummary claims = claimSummaryRepository.findByPolicyId(policy.getId()).orElse(null);
        PolicySnapshot snapshot = PolicySnapshot.from(policy, claims, today);

        RuleEvaluationResult result = ruleEngineService.evaluate(snapshot, ruleSet);

        RenewalCase renewalCase = RenewalCase.builder()
                .batchRun(batchRun)
                .policy(policy)
                .ruleSet(ruleSet)
                .decision(result.getFinalDecision())
                .decisionReason(result.getDecisionReason())
                .build();

        for (RuleExecutionEntry entry : result.getExecutionLog()) {
            renewalCase.getExecutionLogs().add(RenewalRuleExecutionLog.builder()
                    .renewalCase(renewalCase)
                    .ruleId(entry.getRuleId())
                    .ruleCode(entry.getRuleCode())
                    .result(entry.isPassed() ? "PASS" : "FAIL")
                    .evaluatedValue(entry.getEvaluatedValue())
                    .outcomeApplied(entry.getOutcomeApplied())
                    .build());
        }

        renewalCaseRepository.save(renewalCase);

        if ("REFER".equals(result.getFinalDecision())) {
            referralCaseRepository.save(ReferralCase.builder()
                    .renewalCase(renewalCase)
                    .build());
        } else if ("ELIGIBLE".equals(result.getFinalDecision())) {
            // Placeholder premium calc - a real implementation would call the rating engine.
            renewalOfferRepository.save(RenewalOffer.builder()
                    .renewalCase(renewalCase)
                    .offeredPremium(policy.getTotalPremium())
                    .loadingPct(BigDecimal.ZERO)
                    .build());
        }

        return result.getFinalDecision();
    }
}
