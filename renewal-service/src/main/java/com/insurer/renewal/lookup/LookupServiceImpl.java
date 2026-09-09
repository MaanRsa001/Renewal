package com.insurer.renewal.lookup;

import com.insurer.renewal.common.BusinessRuleViolationException;
import com.insurer.renewal.lookup.dto.LookupItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LookupServiceImpl implements LookupService {

    private final SystemCodeRepository systemCodeRepository;

    @Override
    public List<LookupItemDto> getCodes(String codeType) {
        return systemCodeRepository.findByCodeTypeAndActiveTrueOrderBySortOrderAsc(codeType).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Map<String, List<LookupItemDto>> getAllCodes() {
        return systemCodeRepository.findByActiveTrueOrderByCodeTypeAscSortOrderAsc().stream()
                .collect(Collectors.groupingBy(
                        SystemCode::getCodeType,
                        Collectors.mapping(this::toDto, Collectors.toList())));
    }

    @Override
    public void requireValid(String codeType, String codeValue) {
        boolean valid = systemCodeRepository.findByCodeTypeAndCodeValue(codeType, codeValue)
                .filter(SystemCode::isActive)
                .isPresent();
        if (!valid) {
            throw new BusinessRuleViolationException(
                    "'" + codeValue + "' is not a valid/active " + codeType + " code.");
        }
    }

    @Override
    public String displayNameOf(String codeType, String codeValue) {
        return systemCodeRepository.findByCodeTypeAndCodeValue(codeType, codeValue)
                .map(SystemCode::getDisplayName)
                .orElse(codeValue);
    }

    private LookupItemDto toDto(SystemCode sc) {
        return LookupItemDto.builder()
                .value(sc.getCodeValue())
                .label(sc.getDisplayName())
                .styleHint(sc.getStyleHint())
                .sortOrder(sc.getSortOrder())
                .build();
    }
}
