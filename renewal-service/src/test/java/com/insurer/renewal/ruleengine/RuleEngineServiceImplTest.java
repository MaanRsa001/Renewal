package com.insurer.renewal.ruleengine;

import com.insurer.renewal.policy.Policy;
import com.insurer.renewal.policy.PolicyClaimSummary;
import com.insurer.renewal.product.RenewalRule;
import com.insurer.renewal.product.RenewalRuleCondition;
import com.insurer.renewal.product.RenewalRuleSet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mirrors the two worked examples from the design doc (section 11): a clean
 * Motor policy that should auto-renew, and one with high claims/loss ratio
 * that must be referred - while still recording every rule that ran.
 * Category/outcome/operator values here are the same string codes that
 * live in system_code rows (RULE_CATEGORY, RULE_OUTCOME, CONDITION_OPERATOR,
 * LOGICAL_OPERATOR) - see V2__lookup_tables.sql.
 */
class RuleEngineServiceImplTest {

    private final RuleEngineServiceImpl engine = new RuleEngineServiceImpl();

    @Test
    void cleanPolicyIsAutoEligible() {
        RenewalRuleSet ruleSet = buildStandardMotorRuleSet();
        Policy policy = buildPolicy("ACTIVE", 0, BigDecimal.ZERO, false);
        PolicyClaimSummary claims = buildClaims(1, BigDecimal.valueOf(25));

        PolicySnapshot snapshot = PolicySnapshot.from(policy, claims, LocalDate.now());
        RuleEvaluationResult result = engine.evaluate(snapshot, ruleSet);

        assertEquals("ELIGIBLE", result.getFinalDecision());
        assertEquals(4, result.getExecutionLog().size(), "all 4 rules should have been evaluated");
        assertTrue(result.getExecutionLog().stream().allMatch(RuleExecutionEntry::isPassed));
    }

    @Test
    void highClaimsAndLossRatioTriggersReferButStillLogsAllRules() {
        RenewalRuleSet ruleSet = buildStandardMotorRuleSet();
        Policy policy = buildPolicy("ACTIVE", 0, BigDecimal.ZERO, false);
        PolicyClaimSummary claims = buildClaims(5, BigDecimal.valueOf(82));

        PolicySnapshot snapshot = PolicySnapshot.from(policy, claims, LocalDate.now());
        RuleEvaluationResult result = engine.evaluate(snapshot, ruleSet);

        assertEquals("REFER", result.getFinalDecision());
        assertTrue(result.getDecisionReason().contains("CLAIMS_COUNT"),
                "the FIRST terminal rule (claims count, priority 2) should win the decision");
        assertEquals(4, result.getExecutionLog().size());
        assertTrue(result.getExecutionLog().stream()
                .anyMatch(e -> e.getRuleCode().equals("LOSS_RATIO") && !e.isPassed()));
    }

    @Test
    void cancelledPolicyIsImmediatelyIneligible() {
        RenewalRuleSet ruleSet = buildStandardMotorRuleSet();
        Policy policy = buildPolicy("CANCELLED", 0, BigDecimal.ZERO, false);
        PolicyClaimSummary claims = buildClaims(0, BigDecimal.ZERO);

        PolicySnapshot snapshot = PolicySnapshot.from(policy, claims, LocalDate.now());
        RuleEvaluationResult result = engine.evaluate(snapshot, ruleSet);

        assertEquals("INELIGIBLE", result.getFinalDecision());
    }

    @Test
    void fraudFlagRefersRatherThanAutoDeclines() {
        RenewalRuleSet ruleSet = buildStandardMotorRuleSet();
        Policy policy = buildPolicy("ACTIVE", 0, BigDecimal.ZERO, true);
        PolicyClaimSummary claims = buildClaims(0, BigDecimal.ZERO);

        PolicySnapshot snapshot = PolicySnapshot.from(policy, claims, LocalDate.now());
        RuleEvaluationResult result = engine.evaluate(snapshot, ruleSet);

        assertEquals("REFER", result.getFinalDecision(),
                "per design doc section 8: fraud flag should REFER, never auto-decline");
    }

    // ---------- fixtures ----------

    private RenewalRuleSet buildStandardMotorRuleSet() {
        RenewalRuleSet ruleSet = RenewalRuleSet.builder().id(1L).version(1).build();

        RenewalRule statusRule = rule(1L, "POLICY_STATUS", "POLICY_STATUS", 1, "CONTINUE", "INELIGIBLE");
        statusRule.getConditions().add(condition(statusRule, 1, "policy.status", "IN", "[\"ACTIVE\"]"));

        RenewalRule claimsRule = rule(2L, "CLAIMS_COUNT", "CLAIMS", 2, "CONTINUE", "REFER");
        claimsRule.getConditions().add(condition(claimsRule, 1, "claims.count", "LESS_THAN_OR_EQUAL", "2"));

        RenewalRule lossRatioRule = rule(3L, "LOSS_RATIO", "LOSS_RATIO", 3, "CONTINUE", "REFER");
        lossRatioRule.getConditions().add(condition(lossRatioRule, 1, "claims.lossRatioPct", "LESS_THAN_OR_EQUAL", "50"));

        RenewalRule fraudRule = rule(4L, "FRAUD_CHECK", "CUSTOMER", 4, "CONTINUE", "REFER");
        fraudRule.getConditions().add(condition(fraudRule, 1, "policy.fraudFlag", "EQUALS", "false"));

        ruleSet.setRules(List.of(statusRule, claimsRule, lossRatioRule, fraudRule));
        return ruleSet;
    }

    private RenewalRule rule(Long id, String code, String category, int priority, String onPass, String onFail) {
        RenewalRule r = RenewalRule.builder()
                .id(id).ruleCode(code).ruleName(code.replace('_', ' '))
                .category(category).priority(priority)
                .outcomeOnPass(onPass).outcomeOnFail(onFail).active(true)
                .build();
        r.setConditions(new java.util.ArrayList<>());
        return r;
    }

    private RenewalRuleCondition condition(RenewalRule rule, int seq, String field, String op, String value) {
        return RenewalRuleCondition.builder()
                .rule(rule).sequenceNo(seq).fieldName(field).operator(op).value(value)
                .build();
    }

    private Policy buildPolicy(String status, int daysToExpiry, BigDecimal outstandingPremium, boolean fraud) {
        return Policy.builder()
                .id(100L)
                .status(status)
                .expiryDate(LocalDate.now().plusDays(daysToExpiry == 0 ? 45 : daysToExpiry))
                .outstandingPremium(outstandingPremium)
                .totalPremium(BigDecimal.valueOf(1_000_000))
                .sumInsured(BigDecimal.valueOf(5_000_000))
                .fraudFlag(fraud)
                .alreadyRenewed(false)
                .extraAttributes(new java.util.HashMap<>())
                .build();
    }

    private PolicyClaimSummary buildClaims(int count, BigDecimal lossRatioPct) {
        return PolicyClaimSummary.builder()
                .policyId(100L)
                .claimsCount(count)
                .incurredClaims(BigDecimal.ZERO)
                .earnedPremium(BigDecimal.ONE)
                .lossRatioPct(lossRatioPct)
                .build();
    }
}
