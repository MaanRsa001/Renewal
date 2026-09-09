package com.insurer.renewal.renewal;

import com.insurer.renewal.common.PageResponse;
import com.insurer.renewal.renewal.dto.RenewalBatchCriteria;
import com.insurer.renewal.renewal.dto.RenewalBatchSummaryDto;
import com.insurer.renewal.renewal.dto.RenewalCaseDto;
import com.insurer.renewal.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RenewalBatchController {

    private final RenewalBatchService renewalBatchService;

    @PostMapping("/renewal-batches")
    @PreAuthorize("hasAnyRole('RENEWAL_ADMIN','MANAGER')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RenewalBatchSummaryDto trigger(@Valid @RequestBody RenewalBatchCriteria criteria,
                                           @AuthenticationPrincipal CustomUserDetails principal) {
        // Synchronous for simplicity/traceability in this scope. For very large
        // portfolios, wrap this call with @Async and let getBatchStatus() poll.
        return renewalBatchService.runBatch(criteria, principal.getUserId());
    }

    @GetMapping("/renewal-batches/{id}")
    public RenewalBatchSummaryDto status(@PathVariable Long id) {
        return renewalBatchService.getBatchStatus(id);
    }

    @GetMapping("/renewal-batches/{id}/cases")
    public PageResponse<RenewalCaseDto> cases(@PathVariable Long id, Pageable pageable) {
        return PageResponse.from(renewalBatchService.getCasesForBatch(id, pageable));
    }

    @GetMapping("/renewal-cases/{id}")
    public RenewalCaseDto getCase(@PathVariable Long id) {
        return renewalBatchService.getCase(id);
    }

    @GetMapping("/renewal-cases/{id}/audit-trail")
    public RenewalCaseDto auditTrail(@PathVariable Long id) {
        // Same payload as getCase() - the executionLog field IS the audit trail.
        // Kept as a distinct endpoint to match the design doc's explicit API contract.
        return renewalBatchService.getCase(id);
    }
}
