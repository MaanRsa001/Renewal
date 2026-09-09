package com.insurer.renewal.lookup;

import com.insurer.renewal.lookup.dto.LookupItemDto;

import java.util.List;
import java.util.Map;

public interface LookupService {

    /** All active codes for one type, e.g. "RULE_CATEGORY", ordered for display. */
    List<LookupItemDto> getCodes(String codeType);

    /** Every active code, grouped by type - lets the frontend preload everything in one call. */
    Map<String, List<LookupItemDto>> getAllCodes();

    /** Throws BusinessRuleViolationException if the value isn't a known, active code of that type. */
    void requireValid(String codeType, String codeValue);

    String displayNameOf(String codeType, String codeValue);
}
