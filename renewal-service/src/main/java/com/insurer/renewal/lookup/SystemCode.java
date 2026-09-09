package com.insurer.renewal.lookup;

import jakarta.persistence.*;
import lombok.*;

/**
 * Generic reference-data row backing every "enum-like" classification in the
 * system (rule categories, outcomes, operators, statuses, roles, ...).
 * Replaces hardcoded Java enums so new codes - or a relabeled/reordered/
 * deactivated existing one - can be rolled out via data, not a redeploy.
 *
 * codeType groups related codes, e.g. "RULE_CATEGORY", "REFERRAL_STATUS".
 * codeValue is the stable machine value stored on business rows and
 * referenced in code (e.g. service-layer checks like
 * status.equals("ACTIVE")) - so values themselves are still a fixed
 * contract the code understands, but the *catalog* of what's valid,
 * its display label, ordering, and active/inactive state are fully
 * data-driven and editable without a code change.
 */
@Entity
@Table(name = "system_code", uniqueConstraints = @UniqueConstraint(columnNames = {"code_type", "code_value"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_type", nullable = false, length = 40)
    private String codeType;

    @Column(name = "code_value", nullable = false, length = 40)
    private String codeValue;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /** Optional UI hint (GREEN / AMBER / RED / GRAY) so status colors are data-driven too. */
    @Column(name = "style_hint", length = 10)
    private String styleHint;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
