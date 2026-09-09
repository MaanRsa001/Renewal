package com.insurer.renewal.ruleengine;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RuleEvaluationResult {

    /** system_code(codeType=RULE_OUTCOME) value. Defaults to ELIGIBLE when every rule is CONTINUE. */
    private String finalDecision = "ELIGIBLE";
    private String decisionReason = "All eligibility checks passed.";
    private boolean decisionLocked = false;
    private final List<RuleExecutionEntry> executionLog = new ArrayList<>();

    public void addEntry(RuleExecutionEntry entry) {
        executionLog.add(entry);
    }

    /**
     * The first rule that produces a terminal outcome (anything but CONTINUE)
     * wins the final decision - but callers keep evaluating remaining rules
     * so the audit trail is complete (see design doc section 3.1).
     */
    public void applyOutcomeIfTerminal(String outcome, String reason) {
        if (decisionLocked) return;
        if (!"CONTINUE".equals(outcome)) {
            this.finalDecision = outcome;
            this.decisionReason = reason;
            this.decisionLocked = true;
        }
    }
}
