package com.insurer.renewal.product;

import com.insurer.renewal.common.BusinessRuleViolationException;
import com.insurer.renewal.common.ResourceNotFoundException;
import com.insurer.renewal.lookup.CodeTypes;
import com.insurer.renewal.lookup.LookupService;
import com.insurer.renewal.product.dto.ConditionDto;
import com.insurer.renewal.product.dto.CreateRuleSetRequest;
import com.insurer.renewal.product.dto.RuleDto;
import com.insurer.renewal.product.dto.RuleSetDto;
import com.insurer.renewal.product.dto.UpdateRuleSetRequest;
import com.insurer.renewal.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RuleConfigServiceImpl implements RuleConfigService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";

    private final ProductRepository productRepository;
    private final CountryRepository countryRepository;
    private final LineOfBusinessRepository lobRepository;
    private final RenewalRuleSetRepository ruleSetRepository;
    private final RenewalRuleRepository ruleRepository;
    private final LookupService lookupService;

    @Override
    public RuleSetDto createDraftRuleSet(CreateRuleSetRequest request, Long createdByUserId) {
        Product product = productRepository.findByCode(request.getProductCode())
                .orElseThrow(() -> new ResourceNotFoundException("Unknown product code: " + request.getProductCode()));
        Country country = countryRepository.findByCode(request.getCountryCode())
                .orElseThrow(() -> new ResourceNotFoundException("Unknown country code: " + request.getCountryCode()));

        LineOfBusiness lob = null;
        if (request.getLobCode() != null && !request.getLobCode().isBlank()) {
            lob = lobRepository.findAll().stream()
                    .filter(l -> l.getCode().equalsIgnoreCase(request.getLobCode()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Unknown LOB code: " + request.getLobCode()));
        }

        int nextVersion = ruleSetRepository.findByProduct_IdAndCountry_Id(product.getId(), country.getId())
                .stream().mapToInt(RenewalRuleSet::getVersion).max().orElse(0) + 1;

        RenewalRuleSet ruleSet = RenewalRuleSet.builder()
                .product(product)
                .country(country)
                .lineOfBusiness(lob)
                .version(nextVersion)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .status(STATUS_DRAFT)
                .createdBy(createdByUserId)
                .build();

        return toDto(ruleSetRepository.save(ruleSet));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleSetDto> listRuleSets(String productCode, String lobCode, String countryCode) {
        return ruleSetRepository.findAll().stream()
                .filter(rs -> productCode == null || rs.getProduct().getCode().equalsIgnoreCase(productCode))
                .filter(rs -> countryCode == null || rs.getCountry().getCode().equalsIgnoreCase(countryCode))
                .filter(rs -> lobCode == null ||
                        (rs.getLineOfBusiness() != null && rs.getLineOfBusiness().getCode().equalsIgnoreCase(lobCode)))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RuleSetDto getRuleSet(Long ruleSetId) {
        return toDto(findRuleSetOrThrow(ruleSetId));
    }

    @Override
    public RuleSetDto activateRuleSet(Long ruleSetId) {
        RenewalRuleSet ruleSet = findRuleSetOrThrow(ruleSetId);
        if (ruleSet.getRules().isEmpty()) {
            throw new BusinessRuleViolationException("Cannot activate a rule set with no rules.");
        }

        // deactivate any prior active version for the same product/lob/country
        ruleSetRepository.findByProduct_IdAndCountry_Id(ruleSet.getProduct().getId(), ruleSet.getCountry().getId())
                .stream()
                .filter(rs -> STATUS_ACTIVE.equals(rs.getStatus()) && !rs.getId().equals(ruleSet.getId()))
                .forEach(rs -> {
                    rs.setStatus(STATUS_INACTIVE);
                    ruleSetRepository.save(rs);
                });

        ruleSet.setStatus(STATUS_ACTIVE);
        return toDto(ruleSetRepository.save(ruleSet));
    }

    @Override
    public RuleDto addRule(Long ruleSetId, RuleDto ruleDto) {
        RenewalRuleSet ruleSet = findRuleSetOrThrow(ruleSetId);
        if (!STATUS_DRAFT.equals(ruleSet.getStatus())) {
            throw new BusinessRuleViolationException("Rules can only be added to a DRAFT rule set. Create a new version instead.");
        }

        lookupService.requireValid(CodeTypes.RULE_CATEGORY, ruleDto.getCategory());
        lookupService.requireValid(CodeTypes.RULE_OUTCOME, ruleDto.getOutcomeOnPass());
        lookupService.requireValid(CodeTypes.RULE_OUTCOME, ruleDto.getOutcomeOnFail());

        RenewalRule rule = RenewalRule.builder()
                .ruleSet(ruleSet)
                .ruleCode(ruleDto.getRuleCode())
                .ruleName(ruleDto.getRuleName())
                .category(ruleDto.getCategory())
                .priority(ruleDto.getPriority())
                .outcomeOnPass(ruleDto.getOutcomeOnPass())
                .outcomeOnFail(ruleDto.getOutcomeOnFail())
                .active(true)
                .build();

        if (ruleDto.getConditions() != null) {
            for (ConditionDto c : ruleDto.getConditions()) {
                lookupService.requireValid(CodeTypes.CONDITION_OPERATOR, c.getOperator());
                lookupService.requireValid(CodeTypes.LOGICAL_OPERATOR, c.getLogicalOperator());

                rule.getConditions().add(RenewalRuleCondition.builder()
                        .rule(rule)
                        .sequenceNo(c.getSequenceNo())
                        .fieldName(c.getFieldName())
                        .operator(c.getOperator())
                        .value(c.getValue())
                        .logicalOperator(c.getLogicalOperator())
                        .build());
            }
        }

        ruleSet.getRules().add(rule);
        ruleSetRepository.save(ruleSet);
        return toRuleDto(rule);
    }

    @Override
    public void deactivateRule(Long ruleId) {
        RenewalRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found: " + ruleId));
        rule.setActive(false);
        ruleRepository.save(rule);
    }

    private RenewalRuleSet findRuleSetOrThrow(Long id) {
        return ruleSetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule set not found: " + id));
    }

    private RuleSetDto toDto(RenewalRuleSet rs) {
        return RuleSetDto.builder()
                .id(rs.getId())
                .productCode(rs.getProduct().getCode())
                .lobCode(rs.getLineOfBusiness() != null ? rs.getLineOfBusiness().getCode() : null)
                .countryCode(rs.getCountry().getCode())
                .version(rs.getVersion())
                .effectiveFrom(rs.getEffectiveFrom())
                .effectiveTo(rs.getEffectiveTo())
                .status(rs.getStatus())
                .rules(rs.getRules().stream().map(this::toRuleDto).collect(Collectors.toList()))
                .build();
    }

    private RuleDto toRuleDto(RenewalRule r) {
        RuleDto dto = new RuleDto();
        dto.setId(r.getId());
        dto.setRuleCode(r.getRuleCode());
        dto.setRuleName(r.getRuleName());
        dto.setCategory(r.getCategory());
        dto.setPriority(r.getPriority());
        dto.setOutcomeOnPass(r.getOutcomeOnPass());
        dto.setOutcomeOnFail(r.getOutcomeOnFail());
        dto.setActive(r.isActive());
        dto.setConditions(r.getConditions().stream().map(c -> {
            ConditionDto cd = new ConditionDto();
            cd.setId(c.getId());
            cd.setSequenceNo(c.getSequenceNo());
            cd.setFieldName(c.getFieldName());
            cd.setOperator(c.getOperator());
            cd.setValue(c.getValue());
            cd.setLogicalOperator(c.getLogicalOperator());
            return cd;
        }).collect(Collectors.toList()));
        return dto;
    }
    
    @Override
    @Transactional
    public RuleSetDto updateRuleSet(Long ruleSetId, UpdateRuleSetRequest request) {
        RenewalRuleSet ruleSet = findRuleSetOrThrow(ruleSetId);

        Product product = productRepository.findByCode(request.getProductCode())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductCode()));

        Country country = countryRepository.findByCode(request.getCountryCode())
                .orElseThrow(() -> new ResourceNotFoundException("Country not found: " + request.getCountryCode()));

        LineOfBusiness lob = null;

        if (request.getLobCode() != null && !request.getLobCode().isBlank()) {
            lob = lobRepository.findByCode(request.getLobCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Line of business not found: " + request.getLobCode()));
        }

        ruleSet.setProduct(product);
        ruleSet.setLineOfBusiness(lob);
        ruleSet.setCountry(country);
        ruleSet.setEffectiveFrom(request.getEffectiveFrom());
        ruleSet.setEffectiveTo(request.getEffectiveTo());

        return toDto(ruleSetRepository.save(ruleSet));
    }


}
