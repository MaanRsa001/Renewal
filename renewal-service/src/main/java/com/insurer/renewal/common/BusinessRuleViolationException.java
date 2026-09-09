package com.insurer.renewal.common;

/**
 * Thrown when a request is well-formed but violates a business/workflow rule,
 * e.g. attempting to override a referral case that is already closed.
 */
public class BusinessRuleViolationException extends RuntimeException {
    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
