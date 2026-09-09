package com.insurer.renewal.referral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReferralDecisionRequest {

    /** system_code(codeType=REFERRAL_STATUS) - validated against the lookup table server-side. */
    @NotBlank
    private String decision; // APPROVED_WITH_LOADING, DECLINED, ESCALATED

    @NotBlank
    @Size(min = 10, max = 1000)
    private String notes;

    /** Only meaningful when decision = APPROVED_WITH_LOADING */
    private java.math.BigDecimal loadingPct;
}
