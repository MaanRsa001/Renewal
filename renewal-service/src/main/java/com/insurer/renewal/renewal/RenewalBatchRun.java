package com.insurer.renewal.renewal;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "renewal_batch_run")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RenewalBatchRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_date")
    private LocalDateTime runDate;

    @Column(name = "triggered_by")
    private Long triggeredBy;

    /** system_code(codeType=BATCH_STATUS) - RUNNING / COMPLETED / FAILED. */
    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "total_policies_evaluated")
    @Builder.Default
    private Integer totalPoliciesEvaluated = 0;

    @Column(name = "eligible_count")
    @Builder.Default
    private Integer eligibleCount = 0;

    @Column(name = "refer_count")
    @Builder.Default
    private Integer referCount = 0;

    @Column(name = "ineligible_count")
    @Builder.Default
    private Integer ineligibleCount = 0;

    @PrePersist
    void prePersist() {
        if (runDate == null) runDate = LocalDateTime.now();
    }
}
