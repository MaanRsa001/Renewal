package com.insurer.renewal.product.repository;

import com.insurer.renewal.product.RenewalRuleCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RenewalRuleConditionRepository extends JpaRepository<RenewalRuleCondition, Long> {
    List<RenewalRuleCondition> findByRule_IdOrderBySequenceNoAsc(Long ruleId);
}
