package com.insurer.renewal.policy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PolicyClaimSummaryRepository extends JpaRepository<PolicyClaimSummary, Long> {
    Optional<PolicyClaimSummary> findByPolicyId(Long policyId);
}
