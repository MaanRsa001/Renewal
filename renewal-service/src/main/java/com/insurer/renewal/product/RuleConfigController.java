package com.insurer.renewal.product;

import com.insurer.renewal.product.dto.CreateRuleSetRequest;
import com.insurer.renewal.product.dto.RuleDto;
import com.insurer.renewal.product.dto.RuleSetDto;
import com.insurer.renewal.product.dto.UpdateRuleSetRequest;
import com.insurer.renewal.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Rule Designer API - lets Renewal Admins configure eligibility rules
 * per Product + LOB + Country + effective date without touching code.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RuleConfigController {

    private final RuleConfigService ruleConfigService;

    @GetMapping("/rule-sets")
    public List<RuleSetDto> listRuleSets(@RequestParam(required = false) String product,
                                          @RequestParam(required = false) String lob,
                                          @RequestParam(required = false) String country) {
        return ruleConfigService.listRuleSets(product, lob, country);
    }

    @GetMapping("/rule-sets/{id}")
    public RuleSetDto getRuleSet(@PathVariable Long id) {
        return ruleConfigService.getRuleSet(id);
    }

    @PostMapping("/rule-sets")
    @PreAuthorize("hasAnyRole('RENEWAL_ADMIN','MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public RuleSetDto createRuleSet(@Valid @RequestBody CreateRuleSetRequest request,
                                     @AuthenticationPrincipal CustomUserDetails principal) {
        return ruleConfigService.createDraftRuleSet(request, principal.getUserId());
    }

    @PostMapping("/rule-sets/{id}/activate")
    @PreAuthorize("hasAnyRole('RENEWAL_ADMIN','MANAGER')")
    public RuleSetDto activate(@PathVariable Long id) {
        return ruleConfigService.activateRuleSet(id);
    }

    @PostMapping("/rule-sets/{id}/rules")
    @PreAuthorize("hasAnyRole('RENEWAL_ADMIN','MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public RuleDto addRule(@PathVariable Long id, @Valid @RequestBody RuleDto ruleDto) {
        return ruleConfigService.addRule(id, ruleDto);
    }

    @GetMapping("/rule-sets/{id}/rules")
    public List<RuleDto> listRules(@PathVariable Long id) {
        return ruleConfigService.getRuleSet(id).getRules();
    }

    @DeleteMapping("/rules/{ruleId}")
    @PreAuthorize("hasAnyRole('RENEWAL_ADMIN','MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateRule(@PathVariable Long ruleId) {
        ruleConfigService.deactivateRule(ruleId);
    }
    
    @PutMapping("/rule-sets/{id}")
    public RuleSetDto updateRuleSet(
            @PathVariable Long id,
            @RequestBody UpdateRuleSetRequest request) {

        return ruleConfigService.updateRuleSet(id, request);
    }

}
