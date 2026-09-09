package com.insurer.renewal.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class CreateRuleSetRequest {
    @NotBlank
    private String productCode;

    private String lobCode; // optional - null means "applies to all LOBs"

    @NotBlank
    private String countryCode;

    @NotNull
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}
