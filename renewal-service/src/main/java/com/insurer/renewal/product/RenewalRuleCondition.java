package com.insurer.renewal.product;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "renewal_rule_condition")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RenewalRuleCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private RenewalRule rule;

    @Column(name = "sequence_no", nullable = false)
    private Integer sequenceNo;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    /** system_code(codeType=CONDITION_OPERATOR) - the catalog is data-driven; the
     * actual comparison logic per operator still lives in ConditionEvaluator,
     * since evaluating "BETWEEN" or "IN" is real code, not just a label. */
    @Column(nullable = false, length = 30)
    private String operator;

    /** Literal value, or a JSON array as text for IN / BETWEEN operators. */
    @Column(nullable = false, length = 500)
    private String value;

    /** system_code(codeType=LOGICAL_OPERATOR) - AND / OR. */
    @Column(name = "logical_operator", length = 5)
    @Builder.Default
    private String logicalOperator = "AND";
}
