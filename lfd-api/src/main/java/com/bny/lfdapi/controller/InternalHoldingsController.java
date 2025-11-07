package com.bny.lfdapi.controller;

import com.bny.shared.dto.request.HoldingsRequest;
import com.bny.shared.dto.response.AccountDto;
import com.bny.lfdapi.dto.response.HoldingsResponse;
import com.bny.lfdapi.dto.response.PortfolioSummaryResponse;
import com.bny.lfdapi.service.AccountDataService;
import com.bny.lfdapi.service.HoldingsDataService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal/accounts")
@Validated
public class InternalHoldingsController {

    @Autowired
    private HoldingsDataService holdingsDataService;
    
    @Autowired
    private AccountDataService accountDataService;

    @PostMapping("/{accountId}/holdings")
    public ResponseEntity<HoldingsResponse> getAccountHoldings(
            @PathVariable String accountId,
            @Valid @RequestBody HoldingsRequest request) {
        
        log.info("Get account holdings request received for account: {}", accountId);
        
        request.setAccountId(accountId);
        
        HoldingsResponse response = holdingsDataService.getAccountHoldings(request);
        
        if (response.getResultCode() != null && response.getResultCode() != 0) {
            log.warn("Get account holdings returned non-zero result code: {} - {}", 
                response.getResultCode(), response.getErrorMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountId}/summary")
    public ResponseEntity<PortfolioSummaryResponse> getPortfolioSummary(
            @PathVariable String accountId) {
        
        log.info("Get portfolio summary request received for account: {}", accountId);
        
        PortfolioSummaryResponse response = holdingsDataService.getPortfolioSummary(accountId);
        
        if (response.getResultCode() != null && response.getResultCode() != 0) {
            log.warn("Get portfolio summary returned non-zero result code: {} - {}", 
                response.getResultCode(), response.getErrorMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccountInfo(
            @PathVariable String accountId) {
        
        log.info("Get account info request received for account: {}", accountId);
        
        AccountDto account = accountDataService.getAccountById(accountId);
        
        return ResponseEntity.ok(account);
    }
}
