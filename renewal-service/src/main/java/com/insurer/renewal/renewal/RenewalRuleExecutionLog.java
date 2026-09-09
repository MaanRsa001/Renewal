package com.insurer.renewal.renewal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "renewal_rule_execution_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RenewalRuleExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renewal_case_id", nullable = false)
    private RenewalCase renewalCase;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "rule_code", nullable = false, length = 50)
    private String ruleCode;

    @Column(nullable = false, length = 10)
    private String result; // PASS / FAIL

    @Column(name = "evaluated_value", length = 500)
    private String evaluatedValue;

    /** system_code(codeType=RULE_OUTCOME). */
    @Column(name = "outcome_applied", nullable = false, length = 20)
    private String outcomeApplied;
}
