package com.insurer.renewal.referral;

import com.insurer.renewal.referral.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReferralService {

    Page<ReferralCaseDto> getQueue(String status, String priority, Long assignedTo, Pageable pageable);

    ReferralCaseDto getById(Long referralCaseId);

    ReferralCaseDto assign(Long referralCaseId, Long userId);

    ReferralCaseDto decide(Long referralCaseId, ReferralDecisionRequest request, Long performedByUserId);

    /**
     * The specifically-required workflow: convert a REFER case back to the
     * normal renewal path. Mandatory explanation, restricted to senior roles
     * at the controller layer, fully audited here.
     */
    ReferralCaseDto overrideToNormal(Long referralCaseId, OverrideRequest request, Long performedByUserId);

    List<ReferralHistoryDto> getHistory(Long referralCaseId);
}
