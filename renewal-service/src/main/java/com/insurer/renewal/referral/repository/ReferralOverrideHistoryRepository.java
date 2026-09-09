package com.insurer.renewal.referral.repository;

import com.insurer.renewal.referral.ReferralOverrideHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReferralOverrideHistoryRepository extends JpaRepository<ReferralOverrideHistory, Long> {
    List<ReferralOverrideHistory> findByReferralCase_IdOrderByPerformedAtDesc(Long referralCaseId);
}
