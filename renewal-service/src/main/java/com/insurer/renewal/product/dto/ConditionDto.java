package com.insurer.renewal.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ConditionDto {
    private Long id;

    @NotNull
    private Integer sequenceNo;

    @NotBlank
    private String fieldName;

    /** Validated at runtime against system_code(codeType=CONDITION_OPERATOR). */
    @NotBlank
    private String operator;

    @NotBlank
    private String value;

    /** Validated at runtime against system_code(codeType=LOGICAL_OPERATOR). Defaults to AND. */
    private String logicalOperator = "AND";
}
