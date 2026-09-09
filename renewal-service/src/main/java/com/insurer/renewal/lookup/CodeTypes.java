package com.insurer.renewal.lookup;

/**
 * Constants for the codeType discriminator used in the system_code table.
 * Centralized here so service-layer validation calls and repository
 * lookups reference the same literal rather than retyping strings.
 * The actual VALID VALUES within each type live in the database
 * (system_code rows), not here - this is only the type key.
 */
public final class CodeTypes {
    private CodeTypes() {}

    public static final String RULE_CATEGORY = "RULE_CATEGORY";
    public static final String RULE_OUTCOME = "RULE_OUTCOME";
    public static final String CONDITION_OPERATOR = "CONDITION_OPERATOR";
    public static final String LOGICAL_OPERATOR = "LOGICAL_OPERATOR";
    public static final String RULE_SET_STATUS = "RULE_SET_STATUS";
    public static final String REFERRAL_STATUS = "REFERRAL_STATUS";
    public static final String REFERRAL_PRIORITY = "REFERRAL_PRIORITY";
    public static final String BATCH_STATUS = "BATCH_STATUS";
    public static final String OFFER_STATUS = "OFFER_STATUS";
    public static final String ROLE = "ROLE";
    public static final String POLICY_STATUS = "POLICY_STATUS";
}
