package com.bny.investing.controller;

import com.bny.investing.dto.HoldingsResponseDto;
import com.bny.shared.dto.response.PortfolioSummaryDto;
import com.bny.investing.service.HoldingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@Validated
@RequiredArgsConstructor
public class HoldingsController {
    
    private final HoldingsService holdingsService;
    
    @GetMapping("/{accountId}/holdings")
    public ResponseEntity<HoldingsResponseDto> getAccountHoldings(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        if (size > 1000) {
            size = 1000;
        }
        
        HoldingsResponseDto response = holdingsService.getAccountHoldings(
                accountId, PageRequest.of(page, size));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{accountId}/holdings/summary")
    public ResponseEntity<PortfolioSummaryDto> getPortfolioSummary(
            @PathVariable String accountId) {
        
        PortfolioSummaryDto response = holdingsService.getPortfolioSummary(accountId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{accountId}/holdings/{symbol}/taxlots")
    public ResponseEntity<List<?>> getTaxLots(
            @PathVariable String accountId,
            @PathVariable String symbol) {
        
        return ResponseEntity.ok(Collections.emptyList());
    }
}
