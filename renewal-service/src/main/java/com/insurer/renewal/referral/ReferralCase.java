package com.insurer.renewal.referral;

import com.insurer.renewal.renewal.RenewalCase;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "referral_case")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReferralCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renewal_case_id", nullable = false)
    private RenewalCase renewalCase;

    /** system_code(codeType=REFERRAL_STATUS). */
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "OPEN";

    @Column(name = "assigned_to")
    private Long assignedTo;

    /** system_code(codeType=REFERRAL_PRIORITY). */
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String priority = "MEDIUM";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @OneToMany(mappedBy = "referralCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReferralOverrideHistory> history = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
