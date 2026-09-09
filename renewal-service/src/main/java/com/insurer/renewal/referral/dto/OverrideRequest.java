package com.insurer.renewal.referral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OverrideRequest {

    @NotBlank(message = "An explanation is required to override a referral back to normal renewal.")
    @Size(min = 20, max = 1000, message = "Explanation must be between 20 and 1000 characters.")
    private String explanation;
}
