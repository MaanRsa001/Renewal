package com.insurer.renewal.referral;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "referral_override_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReferralOverrideHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_case_id", nullable = false)
    private ReferralCase referralCase;

    @Column(name = "previous_status", nullable = false, length = 30)
    private String previousStatus;

    @Column(name = "new_status", nullable = false, length = 30)
    private String newStatus;

    /** Mandatory human justification - enforced NOT NULL at DB level too. */
    @Column(nullable = false, length = 1000)
    private String explanation;

    @Column(name = "performed_by", nullable = false)
    private Long performedBy;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    @PrePersist
    void prePersist() {
        if (performedAt == null) performedAt = LocalDateTime.now();
    }
}
