package com.insurer.renewal.product.repository;

import com.insurer.renewal.product.RenewalRuleSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RenewalRuleSetRepository extends JpaRepository<RenewalRuleSet, Long> {

    List<RenewalRuleSet> findByProduct_IdAndCountry_Id(Long productId, Long countryId);

    /**
     * Finds the currently ACTIVE rule set for a product/lob/country combination
     * whose effective window covers the given date. LOB is optional (nullable
     * on the rule set means "applies to all LOBs of this product"). Status is
     * compared as a plain string literal ('ACTIVE') since RuleSetStatus is now
     * a system_code value rather than a Java enum - see CodeTypes.RULE_SET_STATUS.
     */
    @org.springframework.data.jpa.repository.Query("""
        SELECT rs FROM RenewalRuleSet rs
        WHERE rs.product.id = :productId
          AND rs.country.id = :countryId
          AND (:lobId IS NULL OR rs.lineOfBusiness IS NULL OR rs.lineOfBusiness.id = :lobId)
          AND rs.status = 'ACTIVE'
          AND rs.effectiveFrom <= :asOfDate
          AND (rs.effectiveTo IS NULL OR rs.effectiveTo >= :asOfDate)
        ORDER BY rs.version DESC
        """)
    List<RenewalRuleSet> findActiveRuleSets(Long productId, Long lobId, Long countryId, LocalDate asOfDate);

    default Optional<RenewalRuleSet> findActiveRuleSet(Long productId, Long lobId, Long countryId, LocalDate asOfDate) {
        return findActiveRuleSets(productId, lobId, countryId, asOfDate).stream().findFirst();
    }
}
