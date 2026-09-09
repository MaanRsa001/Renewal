package com.insurer.renewal.renewal;

import com.insurer.renewal.policy.Policy;
import com.insurer.renewal.product.RenewalRuleSet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "renewal_case")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RenewalCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_run_id")
    private RenewalBatchRun batchRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_set_id", nullable = false)
    private RenewalRuleSet ruleSet;

    /** system_code(codeType=RULE_OUTCOME) - ELIGIBLE / REFER / INELIGIBLE. */
    @Column(nullable = false, length = 20)
    private String decision;

    @Column(name = "decision_reason", length = 500)
    private String decisionReason;

    @Column(name = "is_overridden", nullable = false)
    @Builder.Default
    private boolean overridden = false;

    @Column(name = "overridden_by")
    private Long overriddenBy;

    @Column(name = "override_reason", length = 1000)
    private String overrideReason;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    @OneToMany(mappedBy = "renewalCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RenewalRuleExecutionLog> executionLogs = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (evaluatedAt == null) evaluatedAt = LocalDateTime.now();
    }
}
