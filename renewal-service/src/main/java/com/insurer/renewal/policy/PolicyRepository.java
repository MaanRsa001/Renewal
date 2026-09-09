package com.insurer.renewal.policy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Optional<Policy> findByPolicyNo(String policyNo);

    @Query("""
        SELECT p FROM Policy p
        WHERE p.status = 'ACTIVE'
          AND p.alreadyRenewed = false
          AND p.expiryDate BETWEEN :fromDate AND :toDate
          AND (:productId IS NULL OR p.product.id = :productId)
          AND (:countryId IS NULL OR p.country.id = :countryId)
        """)
    Page<Policy> findDueForRenewal(LocalDate fromDate, LocalDate toDate,
                                    Long productId, Long countryId, Pageable pageable);
}
