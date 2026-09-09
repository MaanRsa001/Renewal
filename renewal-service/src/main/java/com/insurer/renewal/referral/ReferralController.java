package com.insurer.renewal.referral;

import com.insurer.renewal.common.PageResponse;
import com.insurer.renewal.referral.dto.*;
import com.insurer.renewal.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/referral-cases")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;

    @GetMapping
    public PageResponse<ReferralCaseDto> queue(@RequestParam(required = false) String status,
                                                @RequestParam(required = false) String priority,
                                                @RequestParam(required = false) Long assignedTo,
                                                Pageable pageable) {
        return PageResponse.from(referralService.getQueue(status, priority, assignedTo, pageable));
    }

    @GetMapping("/{id}")
    public ReferralCaseDto getById(@PathVariable Long id) {
        return referralService.getById(id);
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('UNDERWRITER','SENIOR_UNDERWRITER','MANAGER')")
    public ReferralCaseDto assign(@PathVariable Long id, @RequestParam Long userId) {
        return referralService.assign(id, userId);
    }

    @PostMapping("/{id}/decision")
    @PreAuthorize("hasAnyRole('UNDERWRITER','SENIOR_UNDERWRITER','MANAGER')")
    public ReferralCaseDto decide(@PathVariable Long id, @Valid @RequestBody ReferralDecisionRequest request,
                                   @AuthenticationPrincipal CustomUserDetails principal) {
        return referralService.decide(id, request, principal.getUserId());
    }

    /**
     * Referral -> Normal override. Restricted to senior roles: a junior
     * underwriter should never be able to wave through their own referral.
     * Explanation is mandatory (enforced by OverrideRequest bean validation).
     */
    @PostMapping("/{id}/override-to-normal")
    @PreAuthorize("hasAnyRole('SENIOR_UNDERWRITER','MANAGER')")
    public ReferralCaseDto overrideToNormal(@PathVariable Long id, @Valid @RequestBody OverrideRequest request,
                                             @AuthenticationPrincipal CustomUserDetails principal) {
        return referralService.overrideToNormal(id, request, principal.getUserId());
    }

    @GetMapping("/{id}/history")
    public List<ReferralHistoryDto> history(@PathVariable Long id) {
        return referralService.getHistory(id);
    }
}
