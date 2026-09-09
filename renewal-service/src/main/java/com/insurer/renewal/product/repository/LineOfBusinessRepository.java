package com.insurer.renewal.product.repository;

import com.insurer.renewal.product.LineOfBusiness;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LineOfBusinessRepository extends JpaRepository<LineOfBusiness, Long> {
	 Optional<LineOfBusiness> findByCode(String code);
}
