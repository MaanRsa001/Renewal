package com.insurer.renewal.renewal;

import com.insurer.renewal.common.ResourceNotFoundException;
import com.insurer.renewal.policy.Policy;
import com.insurer.renewal.policy.PolicyRepository;
import com.insurer.renewal.product.Product;
import com.insurer.renewal.product.repository.ProductRepository;
import com.insurer.renewal.renewal.dto.*;
import com.insurer.renewal.renewal.repository.RenewalBatchRunRepository;
import com.insurer.renewal.renewal.repository.RenewalCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RenewalBatchServiceImpl implements RenewalBatchService {

    private final PolicyRepository policyRepository;
    private final ProductRepository productRepository;
    private final RenewalBatchRunRepository batchRunRepository;
    private final RenewalCaseRepository renewalCaseRepository;
    private final PolicyRenewalProcessor policyRenewalProcessor;

    @Value("${app.renewal.batch-chunk-size:500}")
    private int chunkSize;

    @Override
    public RenewalBatchSummaryDto runBatch(RenewalBatchCriteria criteria, Long triggeredByUserId) {
        RenewalBatchRun batchRun = batchRunRepository.save(RenewalBatchRun.builder()
                .triggeredBy(triggeredByUserId)
                .status("RUNNING")
                .build());

        Long productId = resolveProductId(criteria.getProductCode());
        int page = 0;
        int evaluated = 0, eligible = 0, refer = 0, ineligible = 0;

        try {
            Page<Policy> pageResult;
            do {
                Pageable pageable = PageRequest.of(page, chunkSize);
                pageResult = policyRepository.findDueForRenewal(
                        criteria.getFromExpiryDate(), criteria.getToExpiryDate(), productId, null, pageable);

                for (Policy policy : pageResult.getContent()) {
                    try {
                        // Delegates to a separate bean so each policy runs in its own
                        // REQUIRES_NEW transaction - one bad policy can't roll back the batch.
                        String decision = policyRenewalProcessor.process(policy, batchRun);
                        evaluated++;
                        switch (decision) {
                            case "ELIGIBLE" -> eligible++;
                            case "REFER" -> refer++;
                            case "INELIGIBLE" -> ineligible++;
                            default -> { /* CONTINUE should never be a final decision */ }
                        }
                    } catch (Exception ex) {
                        log.error("Renewal evaluation failed for policy {}: {}", policy.getPolicyNo(), ex.getMessage(), ex);
                    }
                }
                page++;
            } while (!pageResult.isLast());

            batchRun.setStatus("COMPLETED");
        } catch (Exception ex) {
            batchRun.setStatus("FAILED");
            log.error("Renewal batch {} failed: {}", batchRun.getId(), ex.getMessage(), ex);
        } finally {
            batchRun.setTotalPoliciesEvaluated(evaluated);
            batchRun.setEligibleCount(eligible);
            batchRun.setReferCount(refer);
            batchRun.setIneligibleCount(ineligible);
            batchRunRepository.save(batchRun);
        }

        return toSummaryDto(batchRun);
    }

    @Override
    @Transactional(readOnly = true)
    public RenewalBatchSummaryDto getBatchStatus(Long batchId) {
        return toSummaryDto(findBatchOrThrow(batchId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RenewalCaseDto> getCasesForBatch(Long batchId, Pageable pageable) {
        return renewalCaseRepository.findByBatchRun_Id(batchId, pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public RenewalCaseDto getCase(Long renewalCaseId) {
        return renewalCaseRepository.findById(renewalCaseId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Renewal case not found: " + renewalCaseId));
    }

    private Long resolveProductId(String productCode) {
        if (productCode == null || productCode.isBlank()) return null;
        return productRepository.findByCode(productCode)
                .map(Product::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Unknown product code: " + productCode));
    }

    private RenewalBatchRun findBatchOrThrow(Long id) {
        return batchRunRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch run not found: " + id));
    }

    private RenewalBatchSummaryDto toSummaryDto(RenewalBatchRun b) {
        return RenewalBatchSummaryDto.builder()
                .id(b.getId())
                .runDate(b.getRunDate())
                .status(b.getStatus())
                .totalPoliciesEvaluated(b.getTotalPoliciesEvaluated())
                .eligibleCount(b.getEligibleCount())
                .referCount(b.getReferCount())
                .ineligibleCount(b.getIneligibleCount())
                .build();
    }

    private RenewalCaseDto toDto(RenewalCase c) {
        return RenewalCaseDto.builder()
                .id(c.getId())
                .policyNo(c.getPolicy().getPolicyNo())
                .decision(c.getDecision())
                .decisionReason(c.getDecisionReason())
                .overridden(c.isOverridden())
                .overrideReason(c.getOverrideReason())
                .evaluatedAt(c.getEvaluatedAt())
                .executionLog(c.getExecutionLogs().stream()
                        .map(l -> RuleExecutionLogDto.builder()
                                .ruleCode(l.getRuleCode())
                                .result(l.getResult())
                                .evaluatedValue(l.getEvaluatedValue())
                                .outcomeApplied(l.getOutcomeApplied())
                                .build())
                        .toList())
                .build();
    }
}
