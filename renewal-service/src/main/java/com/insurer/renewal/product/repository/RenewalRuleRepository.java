package com.insurer.renewal.product.repository;

import com.insurer.renewal.product.RenewalRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RenewalRuleRepository extends JpaRepository<RenewalRule, Long> {
    List<RenewalRule> findByRuleSet_IdAndActiveTrueOrderByPriorityAsc(Long ruleSetId);
}
