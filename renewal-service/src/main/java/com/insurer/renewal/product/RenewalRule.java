package com.insurer.renewal.product;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "renewal_rule")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RenewalRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_set_id", nullable = false)
    private RenewalRuleSet ruleSet;

    @Column(name = "rule_code", nullable = false, length = 50)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 150)
    private String ruleName;

    /** system_code(codeType=RULE_CATEGORY) - e.g. CLAIMS, LOSS_RATIO, UNDERWRITING. */
    @Column(nullable = false, length = 30)
    private String category;

    @Column(nullable = false)
    private Integer priority;

    /** system_code(codeType=RULE_OUTCOME) - ELIGIBLE / REFER / INELIGIBLE / CONTINUE. */
    @Column(name = "outcome_on_pass", nullable = false, length = 20)
    private String outcomeOnPass;

    @Column(name = "outcome_on_fail", nullable = false, length = 20)
    private String outcomeOnFail;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RenewalRuleCondition> conditions = new ArrayList<>();
}
