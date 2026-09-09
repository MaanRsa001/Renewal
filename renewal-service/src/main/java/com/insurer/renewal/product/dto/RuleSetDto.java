package com.insurer.renewal.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter @Builder @AllArgsConstructor
public class RuleSetDto {
    private Long id;
    private String productCode;
    private String lobCode;
    private String countryCode;
    private Integer version;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    /** system_code(codeType=RULE_SET_STATUS) - DRAFT / ACTIVE / INACTIVE. */
    private String status;

    private List<RuleDto> rules;
}
