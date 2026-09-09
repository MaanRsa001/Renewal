package com.insurer.renewal.renewal.repository;

import com.insurer.renewal.renewal.RenewalCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RenewalCaseRepository extends JpaRepository<RenewalCase, Long> {
    Page<RenewalCase> findByBatchRun_Id(Long batchRunId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("""
        SELECT rc.decision as decision, COUNT(rc) as cnt
        FROM RenewalCase rc
        WHERE rc.evaluatedAt BETWEEN :from AND :to
          AND (:productCode IS NULL OR rc.policy.product.code = :productCode)
          AND (:countryCode IS NULL OR rc.policy.country.code = :countryCode)
        GROUP BY rc.decision
        """)
    java.util.List<DecisionCountProjection> countByDecisionInRange(
            java.time.LocalDateTime from, java.time.LocalDateTime to, String productCode, String countryCode);

    interface DecisionCountProjection {
        String getDecision();
        Long getCnt();
    }
}
