package com.insurer.renewal.lookup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class LookupItemDto {
    private String value;
    private String label;
    private String styleHint;
    private int sortOrder;
}
