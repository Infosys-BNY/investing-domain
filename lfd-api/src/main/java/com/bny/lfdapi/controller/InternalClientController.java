package com.bny.lfdapi.controller;

import com.bny.shared.dto.request.ClientSearchRequest;
import com.bny.lfdapi.dto.response.AdvisorClientsResponse;
import com.bny.lfdapi.dto.response.ClientSearchResponse;
import com.bny.lfdapi.service.ClientDataService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal")
@Validated
public class InternalClientController {

    @Autowired
    private ClientDataService clientDataService;

    @PostMapping("/clients/search")
    public ResponseEntity<ClientSearchResponse> searchClients(
            @Valid @RequestBody ClientSearchRequest request) {
        
        log.info("Client search request received for advisor: {}", request.getAdvisorId());
        
        ClientSearchResponse response = clientDataService.searchClients(request);
        
        if (response.getResultCode() != null && response.getResultCode() != 0) {
            log.warn("Client search returned non-zero result code: {} - {}", 
                response.getResultCode(), response.getErrorMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/advisors/{advisorId}/clients")
    public ResponseEntity<AdvisorClientsResponse> getAdvisorClients(
            @PathVariable String advisorId,
            @RequestParam(defaultValue = "0") @Min(0) Integer pageOffset,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) Integer pageSize) {
        
        log.info("Get advisor clients request received for advisor: {}, page: {}, size: {}", 
            advisorId, pageOffset, pageSize);
        
        AdvisorClientsResponse response = clientDataService.getAdvisorClients(
            advisorId, pageOffset, pageSize);
        
        if (response.getResultCode() != null && response.getResultCode() != 0) {
            log.warn("Get advisor clients returned non-zero result code: {} - {}", 
                response.getResultCode(), response.getErrorMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
