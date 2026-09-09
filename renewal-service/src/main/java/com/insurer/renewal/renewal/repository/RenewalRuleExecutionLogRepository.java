package com.insurer.renewal.renewal.repository;

import com.insurer.renewal.renewal.RenewalRuleExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RenewalRuleExecutionLogRepository extends JpaRepository<RenewalRuleExecutionLog, Long> {
    List<RenewalRuleExecutionLog> findByRenewalCase_IdOrderByIdAsc(Long renewalCaseId);

    /** Backs the dashboard's "rule hit frequency" widget. */
    @org.springframework.data.jpa.repository.Query("""
        SELECT l.ruleCode as ruleCode, l.outcomeApplied as outcome, COUNT(l) as hits
        FROM RenewalRuleExecutionLog l
        WHERE l.result = 'FAIL'
        GROUP BY l.ruleCode, l.outcomeApplied
        ORDER BY COUNT(l) DESC
        """)
    List<RuleHitProjection> findRuleHitFrequency();

    interface RuleHitProjection {
        String getRuleCode();
        String getOutcome();
        Long getHits();
    }
}
