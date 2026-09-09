package com.insurer.renewal.renewal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class RenewalBatchCriteria {
    @NotNull
    private LocalDate fromExpiryDate;

    @NotNull
    private LocalDate toExpiryDate;

    private String productCode;   // optional filter
    private String countryCode;   // optional filter
}
