package com.insurer.renewal.product;

import com.insurer.renewal.product.dto.CreateRuleSetRequest;
import com.insurer.renewal.product.dto.RuleDto;
import com.insurer.renewal.product.dto.RuleSetDto;
import com.insurer.renewal.product.dto.UpdateRuleSetRequest;

import java.util.List;

public interface RuleConfigService {

    RuleSetDto createDraftRuleSet(CreateRuleSetRequest request, Long createdByUserId);

    List<RuleSetDto> listRuleSets(String productCode, String lobCode, String countryCode);

    RuleSetDto getRuleSet(Long ruleSetId);

    RuleSetDto activateRuleSet(Long ruleSetId);

    RuleDto addRule(Long ruleSetId, RuleDto ruleDto);

    void deactivateRule(Long ruleId);
    
    RuleSetDto updateRuleSet(Long ruleSetId, UpdateRuleSetRequest request);
}
