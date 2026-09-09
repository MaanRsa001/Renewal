package com.insurer.renewal.ruleengine;

import com.insurer.renewal.product.RenewalRuleSet;

public interface RuleEngineService {

    /**
     * Evaluates every active rule in the given rule set (priority order)
     * against the policy snapshot and returns the final decision plus a
     * complete rule-by-rule execution log for audit purposes.
     */
    RuleEvaluationResult evaluate(PolicySnapshot snapshot, RenewalRuleSet ruleSet);
}
