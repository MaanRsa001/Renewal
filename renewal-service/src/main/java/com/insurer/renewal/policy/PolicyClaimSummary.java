package com.insurer.renewal.policy;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "policy_claim_summary")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PolicyClaimSummary {

    @Id
    @Column(name = "policy_id")
    private Long policyId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "policy_id")
    private Policy policy;

    @Column(name = "claims_count", nullable = false)
    private Integer claimsCount;

    @Column(name = "incurred_claims", nullable = false)
    private BigDecimal incurredClaims;

    @Column(name = "earned_premium", nullable = false)
    private BigDecimal earnedPremium;

    @Column(name = "loss_ratio_pct", insertable = false, updatable = false)
    private BigDecimal lossRatioPct; // DB-generated column
}
