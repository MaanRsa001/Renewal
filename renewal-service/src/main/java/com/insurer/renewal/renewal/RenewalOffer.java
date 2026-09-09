package com.insurer.renewal.renewal;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "renewal_offer")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RenewalOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renewal_case_id", nullable = false)
    private RenewalCase renewalCase;

    @Column(name = "offered_premium", nullable = false)
    private BigDecimal offeredPremium;

    @Column(name = "loading_pct")
    @Builder.Default
    private BigDecimal loadingPct = BigDecimal.ZERO;

    /** system_code(codeType=OFFER_STATUS) - GENERATED / SENT / ACCEPTED / EXPIRED. */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "GENERATED";

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @PrePersist
    void prePersist() {
        if (generatedAt == null) generatedAt = LocalDateTime.now();
    }
}
