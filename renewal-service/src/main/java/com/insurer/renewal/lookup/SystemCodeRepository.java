package com.insurer.renewal.lookup;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemCodeRepository extends JpaRepository<SystemCode, Long> {

    List<SystemCode> findByCodeTypeAndActiveTrueOrderBySortOrderAsc(String codeType);

    List<SystemCode> findByActiveTrueOrderByCodeTypeAscSortOrderAsc();

    Optional<SystemCode> findByCodeTypeAndCodeValue(String codeType, String codeValue);
}
