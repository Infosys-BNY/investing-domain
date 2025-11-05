package com.bny.investing.controller;

import com.bny.investing.dto.ClientDto;
import com.bny.investing.dto.ClientSearchRequest;
import com.bny.investing.dto.PaginatedResponse;
import com.bny.investing.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class ClientController {

    private final ClientService clientService;

    @GetMapping("/advisor/{advisorId}/clients")
    public ResponseEntity<PaginatedResponse<ClientDto>> getAdvisorClients(
            @PathVariable String advisorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        PaginatedResponse<ClientDto> response = clientService.getAdvisorClients(advisorId, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clients/search")
    public ResponseEntity<PaginatedResponse<ClientDto>> searchClients(
            @Valid @RequestBody ClientSearchRequest request) {
        
        PaginatedResponse<ClientDto> response = clientService.searchClients(request);
        return ResponseEntity.ok(response);
    }
}
