package com.insurer.renewal.ruleengine;

import com.insurer.renewal.policy.Policy;
import com.insurer.renewal.policy.PolicyClaimSummary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * A flattened, rule-engine-friendly view of a policy at evaluation time.
 * Field names below are the vocabulary rule conditions are written against
 * (see RenewalRuleCondition.fieldName), e.g. "policy.status", "claims.count",
 * "extra.vehicleAgeYears".
 */
public class PolicySnapshot {

    private final Map<String, Object> fields = new HashMap<>();
    private final Long policyId;

    private PolicySnapshot(Long policyId) {
        this.policyId = policyId;
    }

    public static PolicySnapshot from(Policy policy, PolicyClaimSummary claimSummary, LocalDate asOfDate) {
        PolicySnapshot snapshot = new PolicySnapshot(policy.getId());

        snapshot.fields.put("policy.status", policy.getStatus());
        snapshot.fields.put("policy.cancelled", "CANCELLED".equalsIgnoreCase(policy.getStatus()));
        snapshot.fields.put("policy.expiryDate", policy.getExpiryDate().toString());
        snapshot.fields.put("policy.daysToExpiry", ChronoUnit.DAYS.between(asOfDate, policy.getExpiryDate()));
        snapshot.fields.put("policy.outstandingPremium", policy.getOutstandingPremium());
        snapshot.fields.put("policy.totalPremium", policy.getTotalPremium());
        snapshot.fields.put("policy.sumInsured", policy.getSumInsured());
        snapshot.fields.put("policy.fraudFlag", policy.isFraudFlag());
        snapshot.fields.put("policy.alreadyRenewed", policy.isAlreadyRenewed());

        BigDecimal outstandingPct = BigDecimal.ZERO;
        if (policy.getTotalPremium() != null && policy.getTotalPremium().compareTo(BigDecimal.ZERO) > 0
                && policy.getOutstandingPremium() != null) {
            outstandingPct = policy.getOutstandingPremium()
                    .divide(policy.getTotalPremium(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        snapshot.fields.put("policy.outstandingPremiumPct", outstandingPct);

        if (claimSummary != null) {
            snapshot.fields.put("claims.count", claimSummary.getClaimsCount());
            snapshot.fields.put("claims.incurred", claimSummary.getIncurredClaims());
            snapshot.fields.put("claims.earnedPremium", claimSummary.getEarnedPremium());
            BigDecimal lossRatio = claimSummary.getLossRatioPct() != null
                    ? claimSummary.getLossRatioPct()
                    : computeLossRatio(claimSummary);
            snapshot.fields.put("claims.lossRatioPct", lossRatio);
        } else {
            snapshot.fields.put("claims.count", 0);
            snapshot.fields.put("claims.lossRatioPct", BigDecimal.ZERO);
        }

        if (policy.getExtraAttributes() != null) {
            policy.getExtraAttributes().forEach((k, v) -> snapshot.fields.put("extra." + k, v));
        }

        return snapshot;
    }

    private static BigDecimal computeLossRatio(PolicyClaimSummary c) {
        if (c.getEarnedPremium() == null || c.getEarnedPremium().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return c.getIncurredClaims().divide(c.getEarnedPremium(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public Object get(String fieldName) {
        return fields.get(fieldName);
    }

    public Long getPolicyId() {
        return policyId;
    }

    public Map<String, Object> asMap() {
        return fields;
    }
}
