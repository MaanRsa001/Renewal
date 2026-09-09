package com.insurer.renewal.ruleengine;

import com.insurer.renewal.product.RenewalRule;
import com.insurer.renewal.product.RenewalRuleCondition;
import com.insurer.renewal.product.RenewalRuleSet;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class RuleEngineServiceImpl implements RuleEngineService {

    @Override
    public RuleEvaluationResult evaluate(PolicySnapshot snapshot, RenewalRuleSet ruleSet) {
        RuleEvaluationResult result = new RuleEvaluationResult();

        List<RenewalRule> rules = ruleSet.getRules().stream()
                .filter(RenewalRule::isActive)
                .sorted(Comparator.comparing(RenewalRule::getPriority))
                .toList();

        for (RenewalRule rule : rules) {
            boolean passed = evaluateRule(rule, snapshot);
            String outcome = passed ? rule.getOutcomeOnPass() : rule.getOutcomeOnFail();

            result.addEntry(new RuleExecutionEntry(
                    rule.getId(), rule.getRuleCode(), rule.getRuleName(),
                    passed, describeEvaluatedValue(rule, snapshot), outcome));

            if (!"CONTINUE".equals(outcome)) {
                String reason = String.format("Rule '%s' (%s) %s -> %s",
                        rule.getRuleName(), rule.getRuleCode(),
                        passed ? "passed" : "failed", outcome);
                result.applyOutcomeIfTerminal(outcome, reason);
            }
        }

        return result;
    }

    /**
     * Evaluates a rule's condition chain left-to-right honoring each
     * condition's own logicalOperator to join it to the NEXT condition.
     * e.g. conditions [A(AND), B(OR), C] evaluates as ((A AND B) OR C).
     */
    private boolean evaluateRule(RenewalRule rule, PolicySnapshot snapshot) {
        List<RenewalRuleCondition> conditions = rule.getConditions().stream()
                .sorted((a, b) -> Integer.compare(a.getSequenceNo(), b.getSequenceNo()))
                .toList();

        if (conditions.isEmpty()) {
            return true; // a rule with no conditions always passes (e.g. a pure gating rule)
        }

        boolean accumulator = evaluateSingle(conditions.get(0), snapshot);

        for (int i = 1; i < conditions.size(); i++) {
            RenewalRuleCondition previous = conditions.get(i - 1);
            boolean next = evaluateSingle(conditions.get(i), snapshot);
            String joiner = previous.getLogicalOperator() == null ? "AND" : previous.getLogicalOperator();
            accumulator = "AND".equals(joiner) ? (accumulator && next) : (accumulator || next);
        }

        return accumulator;
    }

    private boolean evaluateSingle(RenewalRuleCondition condition, PolicySnapshot snapshot) {
        Object actual = snapshot.get(condition.getFieldName());
        return ConditionEvaluator.evaluate(actual, condition.getOperator(), condition.getValue());
    }

    private String describeEvaluatedValue(RenewalRule rule, PolicySnapshot snapshot) {
        StringBuilder sb = new StringBuilder();
        rule.getConditions().stream()
                .sorted((a, b) -> Integer.compare(a.getSequenceNo(), b.getSequenceNo()))
                .forEach(c -> sb.append(c.getFieldName()).append("=")
                        .append(snapshot.get(c.getFieldName())).append("; "));
        return sb.toString();
    }
}
