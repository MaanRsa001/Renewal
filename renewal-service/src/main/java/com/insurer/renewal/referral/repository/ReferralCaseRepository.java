package com.insurer.renewal.referral.repository;

import com.insurer.renewal.referral.ReferralCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReferralCaseRepository extends JpaRepository<ReferralCase, Long> {

    Optional<ReferralCase> findByRenewalCase_Id(Long renewalCaseId);

    /** status/priority are system_code(codeType=REFERRAL_STATUS / REFERRAL_PRIORITY) values. */
    @Query("""
        SELECT rc FROM ReferralCase rc
        WHERE (:status IS NULL OR rc.status = :status)
          AND (:priority IS NULL OR rc.priority = :priority)
          AND (:assignedTo IS NULL OR rc.assignedTo = :assignedTo)
        """)
    Page<ReferralCase> search(String status, String priority, Long assignedTo, Pageable pageable);

    long countByStatusAndAssignedTo(String status, Long assignedTo);

    @Query("""
        SELECT rc FROM ReferralCase rc
        WHERE rc.status IN ('OPEN','IN_REVIEW')
        """)
    List<ReferralCase> findAllOpenOrInReview();

    @Query("""
        SELECT rc.assignedTo as userId, COUNT(rc) as openCount
        FROM ReferralCase rc
        WHERE rc.status IN ('OPEN','IN_REVIEW') AND rc.assignedTo IS NOT NULL
        GROUP BY rc.assignedTo
        """)
    List<WorkloadProjection> findWorkloadByUnderwriter();

    interface WorkloadProjection {
        Long getUserId();
        Long getOpenCount();
    }
}
