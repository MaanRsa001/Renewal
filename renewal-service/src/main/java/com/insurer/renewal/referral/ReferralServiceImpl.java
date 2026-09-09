package com.insurer.renewal.referral;

import com.insurer.renewal.common.BusinessRuleViolationException;
import com.insurer.renewal.common.ResourceNotFoundException;
import com.insurer.renewal.lookup.CodeTypes;
import com.insurer.renewal.lookup.LookupService;
import com.insurer.renewal.referral.dto.*;
import com.insurer.renewal.referral.repository.ReferralCaseRepository;
import com.insurer.renewal.referral.repository.ReferralOverrideHistoryRepository;
import com.insurer.renewal.renewal.RenewalCase;
import com.insurer.renewal.renewal.repository.RenewalCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ReferralServiceImpl implements ReferralService {

    private static final Set<String> CLOSED_STATUSES = Set.of(
            "APPROVED_NORMAL", "APPROVED_WITH_LOADING", "DECLINED");

    private final ReferralCaseRepository referralCaseRepository;
    private final ReferralOverrideHistoryRepository historyRepository;
    private final RenewalCaseRepository renewalCaseRepository;
    private final LookupService lookupService;

    @Override
    @Transactional(readOnly = true)
    public Page<ReferralCaseDto> getQueue(String status, String priority, Long assignedTo, Pageable pageable) {
        return referralCaseRepository.search(status, priority, assignedTo, pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ReferralCaseDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    @Override
    public ReferralCaseDto assign(Long id, Long userId) {
        ReferralCase referralCase = findOrThrow(id);
        referralCase.setAssignedTo(userId);
        referralCase.setStatus("IN_REVIEW");
        referralCase.setLastUpdatedAt(LocalDateTime.now());
        return toDto(referralCaseRepository.save(referralCase));
    }

    @Override
    public ReferralCaseDto decide(Long id, ReferralDecisionRequest request, Long performedByUserId) {
        lookupService.requireValid(CodeTypes.REFERRAL_STATUS, request.getDecision());

        ReferralCase referralCase = findOrThrow(id);
        assertNotClosed(referralCase);

        String previous = referralCase.getStatus();
        referralCase.setStatus(request.getDecision());
        referralCase.setLastUpdatedAt(LocalDateTime.now());

        recordHistory(referralCase, previous, request.getDecision(), request.getNotes(), performedByUserId);
        referralCaseRepository.save(referralCase);

        if ("APPROVED_WITH_LOADING".equals(request.getDecision())) {
            RenewalCase renewalCase = referralCase.getRenewalCase();
            renewalCase.setDecision("ELIGIBLE");
            renewalCase.setDecisionReason("Approved by underwriter with " + request.getLoadingPct() + "% loading. " + request.getNotes());
            renewalCaseRepository.save(renewalCase);
        }

        return toDto(referralCase);
    }

    @Override
    public ReferralCaseDto overrideToNormal(Long id, OverrideRequest request, Long performedByUserId) {
        ReferralCase referralCase = findOrThrow(id);
        assertNotClosed(referralCase);

        String previous = referralCase.getStatus();
        referralCase.setStatus("APPROVED_NORMAL");
        referralCase.setLastUpdatedAt(LocalDateTime.now());

        recordHistory(referralCase, previous, "APPROVED_NORMAL", request.getExplanation(), performedByUserId);
        referralCaseRepository.save(referralCase);

        // Flip the underlying renewal decision back to ELIGIBLE, tagged as a human override
        // so downstream reporting can distinguish auto-eligible from overridden-eligible.
        RenewalCase renewalCase = referralCase.getRenewalCase();
        renewalCase.setDecision("ELIGIBLE");
        renewalCase.setOverridden(true);
        renewalCase.setOverriddenBy(performedByUserId);
        renewalCase.setOverrideReason(request.getExplanation());
        renewalCaseRepository.save(renewalCase);

        return toDto(referralCase);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReferralHistoryDto> getHistory(Long referralCaseId) {
        return historyRepository.findByReferralCase_IdOrderByPerformedAtDesc(referralCaseId).stream()
                .map(h -> ReferralHistoryDto.builder()
                        .previousStatus(h.getPreviousStatus())
                        .newStatus(h.getNewStatus())
                        .explanation(h.getExplanation())
                        .performedBy(h.getPerformedBy())
                        .performedAt(h.getPerformedAt())
                        .build())
                .toList();
    }

    private void assertNotClosed(ReferralCase referralCase) {
        if (CLOSED_STATUSES.contains(referralCase.getStatus())) {
            throw new BusinessRuleViolationException(
                    "Referral case " + referralCase.getId() + " is already closed (" + referralCase.getStatus() + ").");
        }
    }

    private void recordHistory(ReferralCase referralCase, String previous, String next,
                                String explanation, Long performedBy) {
        referralCase.getHistory().add(ReferralOverrideHistory.builder()
                .referralCase(referralCase)
                .previousStatus(previous)
                .newStatus(next)
                .explanation(explanation)
                .performedBy(performedBy)
                .build());
    }

    private ReferralCase findOrThrow(Long id) {
        return referralCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referral case not found: " + id));
    }

    private ReferralCaseDto toDto(ReferralCase rc) {
        RenewalCase renewalCase = rc.getRenewalCase();
        return ReferralCaseDto.builder()
                .id(rc.getId())
                .policyNo(renewalCase.getPolicy().getPolicyNo())
                .productCode(renewalCase.getPolicy().getProduct().getCode())
                .decisionReason(renewalCase.getDecisionReason())
                .status(rc.getStatus())
                .priority(rc.getPriority())
                .assignedTo(rc.getAssignedTo())
                .createdAt(rc.getCreatedAt())
                .lastUpdatedAt(rc.getLastUpdatedAt())
                .build();
    }
}
