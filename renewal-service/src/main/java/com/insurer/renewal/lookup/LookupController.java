package com.insurer.renewal.lookup;

import com.insurer.renewal.lookup.dto.LookupItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Backs every dropdown/select in the Angular app. The frontend calls
 * GET /lookups/all once at startup and caches the result, rather than
 * hardcoding category/status/operator/role lists in TypeScript -
 * so adding or relabeling a code is a data change (a new system_code
 * row), not a frontend or backend redeploy.
 */
@RestController
@RequestMapping("/api/v1/lookups")
@RequiredArgsConstructor
public class LookupController {

    private final LookupService lookupService;

    @GetMapping("/all")
    public Map<String, List<LookupItemDto>> all() {
        return lookupService.getAllCodes();
    }

    @GetMapping("/{codeType}")
    public List<LookupItemDto> byType(@PathVariable String codeType) {
        return lookupService.getCodes(codeType);
    }
}
