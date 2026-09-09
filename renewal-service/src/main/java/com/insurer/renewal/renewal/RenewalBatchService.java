package com.insurer.renewal.renewal;

import com.insurer.renewal.renewal.dto.RenewalBatchCriteria;
import com.insurer.renewal.renewal.dto.RenewalBatchSummaryDto;
import com.insurer.renewal.renewal.dto.RenewalCaseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RenewalBatchService {

    RenewalBatchSummaryDto runBatch(RenewalBatchCriteria criteria, Long triggeredByUserId);

    RenewalBatchSummaryDto getBatchStatus(Long batchId);

    Page<RenewalCaseDto> getCasesForBatch(Long batchId, Pageable pageable);

    RenewalCaseDto getCase(Long renewalCaseId);
}
