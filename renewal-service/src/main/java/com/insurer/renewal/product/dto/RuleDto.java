package com.insurer.renewal.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class RuleDto {
    private Long id;

    @NotBlank
    private String ruleCode;

    @NotBlank
    private String ruleName;

    /** Validated at runtime against system_code(codeType=RULE_CATEGORY). */
    @NotBlank
    private String category;

    @NotNull
    private Integer priority;

    /** Validated at runtime against system_code(codeType=RULE_OUTCOME). */
    @NotBlank
    private String outcomeOnPass;

    @NotBlank
    private String outcomeOnFail;

    private boolean active = true;

    @Valid
    private List<ConditionDto> conditions;
}
